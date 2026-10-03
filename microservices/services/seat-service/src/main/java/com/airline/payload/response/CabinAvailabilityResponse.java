package com.airline.payload.response;

import com.airline.enums.CabinClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CabinAvailabilityResponse {

    private Long cabinId;
    private Long flightInstanceId;
    private CabinClass cabinClass;

    private Integer totalSeats;
    private Integer availableSeats;
    private Integer occupiedSeats;
    private Integer blockedSeats;
    private Integer reservedSeats;

}