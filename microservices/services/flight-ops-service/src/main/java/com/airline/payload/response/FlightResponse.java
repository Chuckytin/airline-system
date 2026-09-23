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
public class FlightResponse {

    private Long id;
    private String flightNumber;
    private Long airlineId;
    private Long departureAirportId;
    private Long arrivalAirportId;
    private Integer estimatedDurationMinutes;
    private Integer distanceKm;

    private Instant createdAt;
    private Instant updatedAt;

}