package com.coder.pema.posmicroservice.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset; // Import ZoneOffset
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.coder.pema.posmicroservice.dto.GetSaleResponse;
import com.coder.pema.posmicroservice.dto.Item;
import com.coder.pema.posmicroservice.entity.Sales;
import com.coder.pema.posmicroservice.entity.SalesItem;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

@Repository
public class SaleDAOImpl implements SaleDAO {

    private final EntityManager entityManager;

    @Autowired
    public SaleDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Sales saveSales(Sales sales) {
        try {
            return entityManager.merge(sales);
        } catch (Exception e) {
            System.out.println("Failed to save sale " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean saveSalesItem(SalesItem salesItem) {
        try {
            entityManager.merge(salesItem);
            return true;
        } catch (Exception e) {
            System.out.println("failed to save salesitem " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public GetSaleResponse getSales(String saleId) {
        try {
            TypedQuery<Sales> query = entityManager.createQuery(
                    "SELECT s FROM Sales s LEFT JOIN FETCH s.items WHERE s.id = :saleId", Sales.class);
            query.setParameter("saleId", Long.valueOf(saleId));
            Sales sale = query.getSingleResult();

            GetSaleResponse response = new GetSaleResponse();
            response.setId(sale.getId());
            response.setCash(sale.getCash());
            response.setDigital(sale.getDigital());
            response.setDate(sale.getDate());
            List<Item> items = sale.getItems().stream().map(item -> {
                Item itemRes = new Item();
                itemRes.setItemId(item.getSalesItemId().getItemId());
                itemRes.setQuantity(item.getQuantity());
                return itemRes;
            }).toList();
            response.setItems(items);
            return response;
        } catch (NoResultException e) {
            System.out.println("No sale found with id " + saleId);
            return null;
        } catch (Exception e) {
            System.out.println("error in getting sale " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Sales> getAllSales() {
        try {
            TypedQuery<Sales> query = entityManager.createQuery("SELECT a FROM Sales a", Sales.class);
            List<Sales> sales = query.getResultList();
            return sales;
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<GetSaleResponse> getAllSalesWithItems() {
        List<Sales> salesList = entityManager.createQuery("SELECT s FROM Sales s LEFT JOIN FETCH s.items", Sales.class)
                .getResultList();
        return salesList.stream().map(sale -> {
            GetSaleResponse response = new GetSaleResponse();
            response.setId(sale.getId());
            response.setCash(sale.getCash());
            response.setDigital(sale.getDigital());
            response.setDate(sale.getDate());

            List<Item> items = sale.getItems().stream().map(item -> {
                Item itemRes = new Item();
                itemRes.setItemId(item.getSalesItemId().getItemId());
                itemRes.setQuantity(item.getQuantity());
                return itemRes;
            }).toList();
            response.setItems(items);
            return response;
        }).toList();
    }

    /**
     * Retrieves a list of Sales entities for a specific local date and timezone
     * offset,
     * including their associated items.
     * Correctly handles timezone conversion to query the UTC-stored OffsetDateTime.
     * 
     * @param localDate   The LocalDate (local date) from the client.
     * @param offsetHours The timezone offset from UTC in hours (e.g., 6 for UTC+6).
     * @return A list of Sales entities.
     */
    @Override
    public List<Sales> findSalesByDate(LocalDate localDate, Integer offsetHours) {
        // Construct the ZoneOffset from the provided hours
        ZoneOffset userZoneOffset = ZoneOffset.ofHours(offsetHours != null ? offsetHours : 0);

        // Define the start and end of the day in the user's *local* timezone
        OffsetDateTime startOfDayInUserZone = OffsetDateTime.of(localDate, LocalTime.MIN, userZoneOffset);
        OffsetDateTime endOfDayInUserZone = OffsetDateTime.of(localDate, LocalTime.MAX, userZoneOffset);

        // Convert these local OffsetDateTimes to UTC OffsetDateTimes for database
        // query.
        // This is crucial because dates are stored in UTC in the database.
        OffsetDateTime startOfDayUtc = startOfDayInUserZone.withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime endOfDayUtc = endOfDayInUserZone.withOffsetSameInstant(ZoneOffset.UTC);

        String jpql = "SELECT s FROM Sales s LEFT JOIN FETCH s.items WHERE s.date >= :startOfDayUtc AND s.date <= :endOfDayUtc";
        TypedQuery<Sales> query = entityManager.createQuery(jpql, Sales.class);
        query.setParameter("startOfDayUtc", startOfDayUtc);
        query.setParameter("endOfDayUtc", endOfDayUtc);

        try {
            List<Sales> sales = query.getResultList();
            return sales;
        } catch (Exception e) {
            System.out.println("Error finding sales by date: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public Optional<Sales> findByIdWithItems(Long saleId) {
        try {
            Sales sales = entityManager.createQuery(
                    "SELECT s FROM Sales s LEFT JOIN FETCH s.items WHERE s.id = :saleId", Sales.class)
                    .setParameter("saleId", saleId)
                    .getSingleResult();
            return Optional.of(sales);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            System.out.println("Error finding sale by ID with items: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
