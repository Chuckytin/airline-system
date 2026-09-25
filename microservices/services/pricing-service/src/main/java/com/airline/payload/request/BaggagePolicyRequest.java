package com.airline.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaggagePolicyRequest {

    @NotNull(message = "Fare ID is mandatory")
    private Long fareId;

    @Min(value = 0, message = "Carry-on pieces cannot be negative")
    private Integer carryOnPieces;

    @Min(value = 0, message = "Carry-on weight cannot be negative")
    private Integer carryOnWeightKg;

    @Min(value = 0, message = "Checked pieces cannot be negative")
    private Integer checkedPieces;

    @Min(value = 0, message = "Checked weight cannot be negative")
    private Integer checkedWeightKg;

    private BigDecimal extraBagPrice;

    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

}