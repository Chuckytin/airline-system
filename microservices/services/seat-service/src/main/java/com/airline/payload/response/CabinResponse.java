package com.airline.payload.response;

import com.airline.enums.CabinClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CabinResponse {

    private Long id;
    private Long flightInstanceId;
    private CabinClass cabinClass;

    private Integer rowStart;
    private Integer rowEnd;
    private String columnLayout;

    private Integer totalSeats;
    private Integer availableSeats;

    private BigDecimal basePrice;
    private String currency;
    private Boolean active;

    private List<SeatMapResponse> seatMaps;
    private List<SeatInstanceResponse> seatInstances;

    private Instant createdAt;
    private Instant updatedAt;

}