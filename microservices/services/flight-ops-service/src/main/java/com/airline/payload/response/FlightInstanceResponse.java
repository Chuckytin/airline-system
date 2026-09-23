package com.airline.payload.response;

import com.airline.enums.FlightStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightInstanceResponse {

    private Long id;
    private Long flightId;
    private Long scheduleId;
    private Long aircraftId;
    private LocalDate flightDate;
    private LocalDateTime departureDateTime;
    private LocalDateTime arrivalDateTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private FlightStatus status;
    private Integer minAdvanceBookingDays;
    private Integer maxAdvanceBookingDays;
    private Boolean active;
    private String formattedDuration;

    private Instant createdAt;
    private Instant updatedAt;

}