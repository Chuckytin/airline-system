package com.airline.payload.request;

import com.airline.enums.CabinClass;
import com.airline.enums.FareType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareRequest {

    @NotNull(message = "Flight ID is mandatory")
    private Long flightId;

    @NotNull(message = "Cabin class is mandatory")
    private CabinClass cabinClass;

    @NotNull(message = "Fare type is mandatory")
    private FareType fareType;

    @NotNull(message = "Base price is mandatory")
    @DecimalMin(value = "0.0", message = "Base price cannot be negative")
    private BigDecimal basePrice;

    @NotNull(message = "Taxes is mandatory")
    @DecimalMin(value = "0.0", message = "Taxes cannot be negative")
    private BigDecimal taxes;

    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @NotNull(message = "Valid from is mandatory")
    private LocalDate validFrom;

    private LocalDate validUntil;

    private Boolean active;

}