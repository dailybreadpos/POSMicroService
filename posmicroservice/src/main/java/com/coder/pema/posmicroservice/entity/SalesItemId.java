package com.coder.pema.posmicroservice.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesItemId implements Serializable {
    
    private Long saleId;
    private Long itemId;

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof SalesItemId)) return false;
        SalesItemId that = (SalesItemId) o;
        return Objects.equals(saleId, that.saleId) && Objects.equals(itemId, that.itemId);

    }

    @Override
    public int hashCode(){
        return Objects.hash(saleId,itemId);
    }

}
