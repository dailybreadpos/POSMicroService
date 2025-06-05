package com.coder.pema.posmicroservice.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import org.springframework.web.reactive.function.client.WebClient; // No longer needed for this communication
// import reactor.core.publisher.Mono; // No longer needed for this communication

import com.coder.pema.posmicroservice.dao.SaleDAO;
import com.coder.pema.posmicroservice.dto.GetSaleResponse;
import com.coder.pema.posmicroservice.dto.Item;
import com.coder.pema.posmicroservice.dto.SalesSaveRequest;
import com.coder.pema.posmicroservice.entity.Sales;
import com.coder.pema.posmicroservice.entity.SalesItem;
import com.coder.pema.posmicroservice.entity.SalesItemId;
import com.coder.pema.posmicroservice.client.InventoryClient; // Import the new Feign client

@Service
public class SalesServiceImpl implements SalesService {

    private final SaleDAO saleDAO;
    private final InventoryClient inventoryClient; // Inject Feign client

    @Autowired
    public SalesServiceImpl(SaleDAO saleDAO, InventoryClient inventoryClient) { // Update constructor
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
            salesEntity.setDate(sales.getDate());
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

    /**
     * Helper method to fetch product details from the Inventory microservice using
     * Feign Client.
     * 
     * @param itemId The ID of the inventory item to fetch.
     * @return A CompletableFuture of an Item (ProductDTO structure) or null if not
     *         found/error.
     */
    private CompletableFuture<Item> fetchProductDetails(Long itemId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Call the Feign client directly
                InventoryClient.InventoryProductResponse invProduct = inventoryClient.getProductDetails(itemId);

                Item itemDetail = new Item();
                itemDetail.setItemId(invProduct.id);
                itemDetail.setName(invProduct.name);
                itemDetail.setDescription(invProduct.description);
                itemDetail.setImage(invProduct.image);
                itemDetail.setPrice(invProduct.price);
                // Optionally set stock or category if needed on the frontend
                // itemDetail.setStock(invProduct.stock);
                // itemDetail.setCategory(invProduct.category);
                return itemDetail;
            } catch (Exception e) {
                System.err.println(
                        "Feign Client Error fetching product details for ID " + itemId + ": " + e.getMessage());
                e.printStackTrace(); // Print stack trace for better debugging

                // Provide a fallback item on error
                Item fallbackItem = new Item();
                fallbackItem.setItemId(itemId);
                fallbackItem.setQuantity(0); // Quantity will be set later by SalesItem
                fallbackItem.setName("Unknown Product (ID: " + itemId + ")");
                fallbackItem.setDescription("Details not available");
                fallbackItem.setPrice(0.0);
                fallbackItem.setImage("https://placehold.co/48x48/CCCCCC/FFFFFF?text=No+Image");
                return fallbackItem;
            }
        });
    }

    /**
     * Converts a Sales entity to GetSaleResponse DTO, enriching SalesItem with
     * product details.
     * 
     * @param sales The Sales entity to convert.
     * @return A CompletableFuture of GetSaleResponse with enriched item details.
     */
    private CompletableFuture<GetSaleResponse> convertSalesToGetSaleResponseWithProductDetails(Sales sales) {
        List<CompletableFuture<Item>> itemDetailsFutures = sales.getItems().stream()
                .map(salesItem -> fetchProductDetails(salesItem.getSalesItemId().getItemId())
                        .thenApply(productDetail -> {
                            // Combine sales item quantity with product details
                            productDetail.setQuantity(salesItem.getQuantity()); // Use setter for quantity
                            return productDetail;
                        }))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(itemDetailsFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    GetSaleResponse response = new GetSaleResponse();
                    response.setId(sales.getId());
                    response.setCash(sales.getCash());
                    response.setDigital(sales.getDigital());
                    response.setDate(sales.getDate());
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
    public List<GetSaleResponse> getSalesByDateWithItems(LocalDate date) {
        List<Sales> salesList = saleDAO.findSalesByDate(date);

        List<CompletableFuture<GetSaleResponse>> futures = salesList.stream()
                .map(this::convertSalesToGetSaleResponseWithProductDetails)
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }
}
