package com.coder.pema.posmicroservice.service;

import java.time.LocalDate;
import java.util.List;

import com.coder.pema.posmicroservice.dto.GetSaleResponse;
import com.coder.pema.posmicroservice.dto.SalesSaveRequest;
import com.coder.pema.posmicroservice.entity.Sales;

public interface SalesService {

    public boolean save(SalesSaveRequest sales);

    public GetSaleResponse getSales(String saleId);

    public List<Sales> getAllSales();

    public List<GetSaleResponse> getAllSalesWithItems();

    /**
     * Retrieves a list of sales (as GetSaleResponse DTOs) for a specific date,
     * including their associated items, considering the timezone offset.
     * 
     * @param date        The LocalDate to filter sales by.
     * @param offsetHours The timezone offset from UTC in hours, for the client.
     * @return A list of GetSaleResponse objects.
     */
    public List<GetSaleResponse> getSalesByDateWithItems(LocalDate date, Integer offsetHours); // Updated signature
}
