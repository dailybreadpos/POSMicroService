package com.coder.pema.posmicroservice.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional; // Import Optional

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

    private final EntityManager entityManager; // Make final as it's injected

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
            e.printStackTrace(); // Print stack trace for better debugging
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
            e.printStackTrace(); // Print stack trace for better debugging
            return false;
        }
    }

    @Override
    public GetSaleResponse getSales(String saleId) {
        try {
            // This query fetches a Sales entity but eager loads items for conversion to
            // GetSaleResponse
            // This method is intended to be used directly by the REST controller if a
            // simple DTO is needed
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
                // No product details here, as this DTO is simple
                return itemRes;
            }).toList();
            response.setItems(items);
            return response;
        } catch (NoResultException e) {
            System.out.println("No sale found with id " + saleId);
            return null;
        } catch (Exception e) {
            System.out.println("error in getting sale " + e.getMessage());
            e.printStackTrace(); // Print stack trace for better debugging
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
            e.printStackTrace(); // Print stack trace for better debugging
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

    @Override
    public List<Sales> findSalesByDate(LocalDate date) {
        OffsetDateTime startOfDay = OffsetDateTime.of(date, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime endOfDay = OffsetDateTime.of(date, LocalTime.MAX, ZoneOffset.UTC);

        String jpql = "SELECT s FROM Sales s LEFT JOIN FETCH s.items WHERE s.date >= :startOfDay AND s.date <= :endOfDay";
        TypedQuery<Sales> query = entityManager.createQuery(jpql, Sales.class);
        query.setParameter("startOfDay", startOfDay);
        query.setParameter("endOfDay", endOfDay);

        try {
            List<Sales> sales = query.getResultList();
            return sales;
        } catch (Exception e) {
            System.out.println("Error finding sales by date: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for better debugging
            return List.of();
        }
    }

    /**
     * Implementation of the new method to find a Sales entity by ID with its items.
     */
    @Override
    public Optional<Sales> findByIdWithItems(Long saleId) {
        try {
            // Use LEFT JOIN FETCH to eagerly load SalesItem entities
            Sales sales = entityManager.createQuery(
                    "SELECT s FROM Sales s LEFT JOIN FETCH s.items WHERE s.id = :saleId", Sales.class)
                    .setParameter("saleId", saleId)
                    .getSingleResult();
            return Optional.of(sales);
        } catch (NoResultException e) {
            // No result means no Sales found for the given ID
            return Optional.empty();
        } catch (Exception e) {
            System.out.println("Error finding sale by ID with items: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty(); // Return empty on other errors
        }
    }
}
