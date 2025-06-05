package com.coder.pema.posmicroservice.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import com.coder.pema.posmicroservice.entity.Sales;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetSaleResponse {
    // private String message;
    // private Sales sale;
    private Long id;
    private float cash;
    private float digital;
    private OffsetDateTime date;
    private List<Item> items;
}
