package com.airline.payload.response;

import com.airline.enums.CabinClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaggagePolicyResponse {

    private Long id;
    private Long fareId;
    private CabinClass cabinClass;
    private Integer carryOnPieces;
    private Integer carryOnWeightKg;
    private Integer checkedPieces;
    private Integer checkedWeightKg;
    private BigDecimal extraBagPrice;
    private String currency;

    private Instant createdAt;
    private Instant updatedAt;

}