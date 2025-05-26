package com.coder.pema.posmicroservice.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import com.coder.pema.posmicroservice.dto.GetSaleResponse;
import com.coder.pema.posmicroservice.dto.Item;
import com.coder.pema.posmicroservice.entity.Sales;
import com.coder.pema.posmicroservice.entity.SalesItem;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

@Repository
public class SaleDAOImpl implements SaleDAO {

    private EntityManager entityManager;

    @Autowired
    public SaleDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Sales saveSales(Sales sales) {

        try {

            return entityManager.merge(sales);
        } catch (Exception e) {
            System.out.println("Failed to save sale " + e);
            return null;
        }
    }

    @Override
    public boolean saveSalesItem(SalesItem salesItem) {
        try {
            entityManager.merge(salesItem);
            return true;
        } catch (Exception e) {
            System.out.println("failed to save salesitem " + e);
            return false;
        }
    }

    @Override
    public GetSaleResponse getSales(String saleId) {
        try {
            // Sales sales = entityManager.find(Sales.class, saleId);
            // if (sales == null) {
            // return null;
            // }
            // return sales;
            Sales sale = entityManager
                    .createQuery("SELECT s FROM Sales s WHERE s.id = :saleId LEFT JOIN FETCH s.items", Sales.class)
                    .getSingleResult();
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
        } catch (Exception e) {
            System.out.println("error " + e);
            return null;
        }
    }

    @Override
    public List<Sales> getAllSales() {
        try {
            TypedQuery<Sales> query = entityManager.createQuery("SELECT a FROM Sales a", Sales.class);
            List<Sales> sales = query.getResultList();
            for (Sales sale : sales) {
                System.out.println("Sale " + sale.getDigital());
            }
            return sales;
        } catch (Exception e) {
            System.out.println("Error " + e);
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
}
