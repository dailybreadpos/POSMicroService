package com.coder.pema.posmicroservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sale_item")
@Getter
@Setter
@NoArgsConstructor
public class SalesItem {
    @EmbeddedId
    private SalesItemId salesItemId;

    @Column(name = "quantity", nullable = false)
    private int quantity;
    // @Column(name = "totalPrice",nullable = false)
    // private float totalPrice;

    @ManyToOne
    @MapsId("saleId")
    @JoinColumn(name = "sale_id")
    private Sales sales;
}
