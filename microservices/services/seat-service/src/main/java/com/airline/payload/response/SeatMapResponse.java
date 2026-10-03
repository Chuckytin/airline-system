package com.airline.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatMapResponse {

    private Long id;
    private Long cabinId;
    private Integer seatRow;
    private String seatLetters;
    private Boolean hasExtraLegroom;
    private Boolean isExitRow;

    private Instant createdAt;
    private Instant updatedAt;

}