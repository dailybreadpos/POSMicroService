package com.coder.pema.posmicroservice.dao;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.coder.pema.posmicroservice.dto.GetSaleResponse;
import com.coder.pema.posmicroservice.entity.Sales;
import com.coder.pema.posmicroservice.entity.SalesItem;

public interface SaleDAO {

    public Sales saveSales(Sales sales);

    public boolean saveSalesItem(SalesItem salesItem);

    public GetSaleResponse getSales(String saleId);

    public List<Sales> getAllSales();

    public List<GetSaleResponse> getAllSalesWithItems();
}
