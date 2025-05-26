package com.coder.pema.posmicroservice.service;

import java.util.List;

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

@Service
public class SalesServiceImpl implements SalesService {

    private SaleDAO saleDAO;

    @Autowired
    public SalesServiceImpl(SaleDAO saleDAO) {
        this.saleDAO = saleDAO;
    }

    @Override
    @Transactional
    public boolean save(SalesSaveRequest sales) {
        try {
            // call inventory service and get item details using itemId or update inventory
            // service data
            Sales salesEntity = new Sales();
            salesEntity.setCash(sales.getCash());
            salesEntity.setDigital(sales.getDigital());
            salesEntity.setGrandTotalPrice(sales.getCash() + sales.getDigital());
            salesEntity.setDate(sales.getDate());
            Sales newSales = saleDAO.saveSales(salesEntity);

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
                    System.out.println("error in saving sales item");

                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("error " + e);
            return false;
        }

    }

    @Override
    public GetSaleResponse getSales(String saleId) {
        return saleDAO.getSales(saleId);
    }

    @Override
    public List<Sales> getAllSales() {
        return saleDAO.getAllSales();
    }

    @Override
    public List<GetSaleResponse> getAllSalesWithItems() {
        return saleDAO.getAllSalesWithItems();
    }

}
