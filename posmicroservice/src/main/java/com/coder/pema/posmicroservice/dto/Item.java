package com.coder.pema.posmicroservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor; // Added for builder pattern in service

@Getter
@Setter
@NoArgsConstructor // Added NoArgsConstructor for cleaner DTO creation
public class Item {
    private Long itemId;
    private int quantity;
    // New fields to hold product details from Inventory
    private String name;
    private String description;
    private String image;
    private double price; // Use double to match Inventory entity's price type
}
