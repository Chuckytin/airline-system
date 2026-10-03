package com.airline.payload.response;

import com.airline.enums.CabinClass;
import com.airline.enums.SeatStatus;
import com.airline.enums.SeatType;
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
public class SeatInstanceResponse {

    private Long id;
    private Long cabinId;
    private CabinClass cabinClass;
    private String seatNumber;
    private Integer seatRow;
    private String columnLetter;
    private SeatType seatType;
    private SeatStatus status;
    private Boolean hasExtraLegroom;
    private Boolean isExitRow;
    private BigDecimal priceModifier;
    private Long bookingId;
    private Long passengerId;

    private Instant createdAt;
    private Instant updatedAt;

}