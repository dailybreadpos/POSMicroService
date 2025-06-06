package com.coder.pema.posmicroservice.service;

import java.time.LocalDate;
import java.time.OffsetDateTime; // Import OffsetDateTime
import java.time.ZoneOffset; // Import ZoneOffset
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coder.pema.posmicroservice.dao.SaleDAO;
import com.coder.pema.posmicroservice.dto.GetSaleResponse;
import com.coder.pema.posmicroservice.dto.Item;
import com.coder.pema.posmicroservice.dto.SalesSaveRequest;
import com.coder.pema.posmicroservice.entity.Sales;
import com.coder.pema.posmicroservice.entity.SalesItem;
import com.coder.pema.posmicroservice.entity.SalesItemId;
import com.coder.pema.posmicroservice.client.InventoryClient;

@Service
public class SalesServiceImpl implements SalesService {

    private final SaleDAO saleDAO;
    private final InventoryClient inventoryClient;

    @Autowired
    public SalesServiceImpl(SaleDAO saleDAO, InventoryClient inventoryClient) {
        this.saleDAO = saleDAO;
        this.inventoryClient = inventoryClient;
    }

    @Override
    @Transactional
    public boolean save(SalesSaveRequest sales) {
        try {
            Sales salesEntity = new Sales();
            salesEntity.setCash(sales.getCash());
            salesEntity.setDigital(sales.getDigital());
            salesEntity.setGrandTotalPrice(sales.getCash() + sales.getDigital());

            // CRITICAL FIX: Construct OffsetDateTime from LocalDateTime and offsetHours
            // This ensures the stored date correctly represents the client's local time +
            // offset
            ZoneOffset clientZoneOffset = ZoneOffset
                    .ofHours(sales.getOffsetHours() != null ? sales.getOffsetHours() : 0);
            OffsetDateTime transactionOffsetDateTime = OffsetDateTime.of(sales.getDate(), clientZoneOffset);
            salesEntity.setDate(transactionOffsetDateTime); // Set the correctly offset date

            Sales newSales = saleDAO.saveSales(salesEntity);

            if (newSales == null) {
                System.out.println("Failed to persist Sales entity.");
                return false;
            }

            for (Item item : sales.getItems()) {
                SalesItemId salesItemId = new SalesItemId();
                SalesItem salesItem = new SalesItem();
                salesItemId.setSaleId(newSales.getId());
                salesItemId.setItemId(item.getItemId());

                salesItem.setQuantity(item.getQuantity());
                salesItem.setSalesItemId(salesItemId);
                salesItem.setSales(newSales);
                boolean error = saleDAO.saveSalesItem(salesItem);

                if (!error) {
                    System.out.println("error in saving sales item for item ID: " + item.getItemId());
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("error saving sales and items: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static class InventoryProductResponse {
        public Long id;
        public String name;
        public String description;
        public String image;
        public double price;
        public int stock;
        public String category;
    }

    private CompletableFuture<Item> fetchProductDetails(Long itemId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                InventoryClient.InventoryProductResponse invProduct = inventoryClient.getProductDetails(itemId);
                Item itemDetail = new Item();
                itemDetail.setItemId(invProduct.id);
                itemDetail.setName(invProduct.name);
                itemDetail.setDescription(invProduct.description);
                itemDetail.setImage(invProduct.image);
                itemDetail.setPrice(invProduct.price);
                return itemDetail;
            } catch (Exception e) {
                System.err.println(
                        "Feign Client Error fetching product details for ID " + itemId + ": " + e.getMessage());
                e.printStackTrace();

                Item fallbackItem = new Item();
                fallbackItem.setItemId(itemId);
                fallbackItem.setQuantity(0);
                fallbackItem.setName("Unknown Product (ID: " + itemId + ")");
                fallbackItem.setDescription("Details not available");
                fallbackItem.setPrice(0.0);
                fallbackItem.setImage("https://placehold.co/48x48/CCCCCC/FFFFFF?text=No+Image");
                return fallbackItem;
            }
        });
    }

    private CompletableFuture<GetSaleResponse> convertSalesToGetSaleResponseWithProductDetails(Sales sales) {
        List<CompletableFuture<Item>> itemDetailsFutures = sales.getItems().stream()
                .map(salesItem -> fetchProductDetails(salesItem.getSalesItemId().getItemId())
                        .thenApply(productDetail -> {
                            productDetail.setQuantity(salesItem.getQuantity());
                            return productDetail;
                        }))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(itemDetailsFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    GetSaleResponse response = new GetSaleResponse();
                    response.setId(sales.getId());
                    response.setCash(sales.getCash());
                    response.setDigital(sales.getDigital());
                    response.setDate(sales.getDate()); // Date is already OffsetDateTime in Sales entity
                    response.setItems(itemDetailsFutures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList()));
                    return response;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public GetSaleResponse getSales(String saleId) {
        try {
            Long idLong = Long.valueOf(saleId);
            Optional<Sales> salesOptional = saleDAO.findByIdWithItems(idLong);

            if (salesOptional.isEmpty()) {
                System.out.println("No sale found with id " + saleId);
                return null;
            }

            Sales salesEntity = salesOptional.get();
            return convertSalesToGetSaleResponseWithProductDetails(salesEntity).join();
        } catch (NumberFormatException e) {
            System.err.println("Invalid saleId format: " + saleId);
            return null;
        } catch (Exception e) {
            System.err.println("Error in getSales with product details: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Sales> getAllSales() {
        return saleDAO.getAllSales();
    }

    @Override
    public List<GetSaleResponse> getAllSalesWithItems() {
        return saleDAO.getAllSalesWithItems();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetSaleResponse> getSalesByDateWithItems(LocalDate date, Integer offsetHours) {
        List<Sales> salesList = saleDAO.findSalesByDate(date, offsetHours);

        List<CompletableFuture<GetSaleResponse>> futures = salesList.stream()
                .map(this::convertSalesToGetSaleResponseWithProductDetails)
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }
}
