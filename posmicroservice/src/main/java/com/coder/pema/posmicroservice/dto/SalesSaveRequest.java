package com.coder.pema.posmicroservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesSaveRequest {

    private float digital;
    private float cash;
    private List<Item> items;
    private LocalDateTime date;

}
