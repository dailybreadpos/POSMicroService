package com.coder.pema.posmicroservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // Changed from OffsetDateTime
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesSaveRequest {
    private float cash;
    private float digital;
    private LocalDateTime date; // Changed to LocalDateTime to represent local time
    private Integer offsetHours; // Added to capture client's timezone offset at time of sale
    private List<Item> items;
}
