package com.coder.pema.posmicroservice.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.coder.pema.posmicroservice.dto.GetSaleResponse;
import com.coder.pema.posmicroservice.entity.Sales;
import com.coder.pema.posmicroservice.entity.SalesItem;

public interface SaleDAO {

    public Sales saveSales(Sales sales);

    public boolean saveSalesItem(SalesItem salesItem);

    public GetSaleResponse getSales(String saleId);

    public List<Sales> getAllSales();

    public List<GetSaleResponse> getAllSalesWithItems();

    /**
     * Retrieves a list of Sales entities for a specific local date and timezone
     * offset,
     * including their associated items.
     * The Sales entity's 'date' field is an OffsetDateTime.
     * 
     * @param date        The LocalDate (local date, not UTC) to filter sales by.
     * @param offsetHours The timezone offset from UTC in hours (e.g., 6 for UTC+6).
     * @return A list of Sales entities.
     */
    public List<Sales> findSalesByDate(LocalDate date, Integer offsetHours); // Updated signature

    public Optional<Sales> findByIdWithItems(Long saleId);
}
