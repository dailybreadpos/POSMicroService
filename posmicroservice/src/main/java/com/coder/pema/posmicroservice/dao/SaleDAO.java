package com.coder.pema.posmicroservice.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional; // Import Optional

import com.coder.pema.posmicroservice.dto.GetSaleResponse;
import com.coder.pema.posmicroservice.entity.Sales;
import com.coder.pema.posmicroservice.entity.SalesItem;

public interface SaleDAO {

    public Sales saveSales(Sales sales);

    public boolean saveSalesItem(SalesItem salesItem);

    // Keep getSales(String saleId) as it is used elsewhere for basic DTO response
    public GetSaleResponse getSales(String saleId);

    public List<Sales> getAllSales();

    public List<GetSaleResponse> getAllSalesWithItems();

    public List<Sales> findSalesByDate(LocalDate date);

    /**
     * Retrieves a Sales entity by its ID, eagerly fetching its associated
     * SalesItems.
     * This is designed to provide the full Sales entity for service-level
     * enrichment.
     * 
     * @param saleId The ID of the sale to retrieve.
     * @return An Optional containing the Sales entity if found, otherwise empty.
     */
    public Optional<Sales> findByIdWithItems(Long saleId); // New method
}
