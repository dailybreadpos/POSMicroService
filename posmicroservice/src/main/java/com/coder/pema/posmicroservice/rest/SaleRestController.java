package com.coder.pema.posmicroservice.rest;

import java.time.LocalDate; // Import LocalDate
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat; // Import DateTimeFormat

import com.coder.pema.posmicroservice.dto.GetSaleResponse;
import com.coder.pema.posmicroservice.dto.SalesResponse;
import com.coder.pema.posmicroservice.dto.SalesSaveRequest;
import com.coder.pema.posmicroservice.entity.Sales;
import com.coder.pema.posmicroservice.service.SalesService;

@RestController
@RequestMapping("/api/pos")
public class SaleRestController {

    private SalesService salesService;

    public SaleRestController(SalesService salesService) {
        this.salesService = salesService;
    }

    @PostMapping("/sales")
    public ResponseEntity<?> save(@RequestBody SalesSaveRequest sales) {
        System.out.println("saving sales " + sales.getDate());
        boolean status = salesService.save(sales);
        SalesResponse salesResponse = new SalesResponse();
        if (!status) {
            System.out.println("Error in saving");
            salesResponse.setStatus("failure");
            salesResponse.setMessage("Error in saving to database");
            return ResponseEntity.internalServerError().body(salesResponse);
        }
        salesResponse.setStatus("success");
        salesResponse.setMessage("successfully recorded sales");

        return ResponseEntity.status(HttpStatus.CREATED).body(salesResponse);
    }

    @GetMapping("/sale")
    public ResponseEntity<?> getASale(@RequestParam String saleId) {
        try {
            GetSaleResponse sale = salesService.getSales(saleId);
            if (sale == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No sale found with id " + saleId);
            }
            return ResponseEntity.status(HttpStatus.OK).body(sale);
        } catch (Exception e) {
            System.out.println("error in getting sale " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/sales")
    public ResponseEntity<?> getAllSales() {
        try {
            List<Sales> sales = salesService.getAllSales();
            if (sales == null || sales.size() == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No sales");
            }
            return ResponseEntity.status(HttpStatus.OK).body(sales);
        } catch (Exception e) {
            System.out.println("Error " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed to get sales");
        }
    }

    @GetMapping("/saless")
    public ResponseEntity<List<GetSaleResponse>> getAllSale() {
        return ResponseEntity.ok().body(salesService.getAllSalesWithItems());
    }

    /**
     * NEW ENDPOINT: Retrieves sales for a specific date, including their items.
     * The date parameter is expected in 'YYYY-MM-DD' format.
     * 
     * @param date The date to filter sales by.
     * @return A list of GetSaleResponse DTOs for the specified date.
     */
    @GetMapping("/sales/by-date") // New API endpoint
    public ResponseEntity<List<GetSaleResponse>> getSalesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<GetSaleResponse> sales = salesService.getSalesByDateWithItems(date);
            if (sales == null || sales.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of()); // Return empty list with 404
            }
            return ResponseEntity.ok().body(sales);
        } catch (Exception e) {
            System.out.println("Error fetching sales by date: " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }
}
