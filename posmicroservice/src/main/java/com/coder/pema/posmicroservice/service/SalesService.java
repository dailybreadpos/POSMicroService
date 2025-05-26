package com.coder.pema.posmicroservice.service;

import java.util.List;

import com.coder.pema.posmicroservice.dto.GetSaleResponse;
import com.coder.pema.posmicroservice.dto.SalesSaveRequest;
import com.coder.pema.posmicroservice.entity.Sales;

public interface SalesService {

    public boolean save(SalesSaveRequest sales);

    public GetSaleResponse getSales(String saleId);

    public List<Sales> getAllSales();

    public List<GetSaleResponse> getAllSalesWithItems();
}
