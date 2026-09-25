package com.airline.payload.response;

import com.airline.enums.CabinClass;
import com.airline.enums.FareType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareResponse {

    private Long id;
    private Long flightId;
    private CabinClass cabinClass;
    private FareType fareType;
    private BigDecimal basePrice;
    private BigDecimal taxes;
    private BigDecimal totalPrice;
    private String currency;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private Boolean active;

    private Instant createdAt;
    private Instant updatedAt;

}