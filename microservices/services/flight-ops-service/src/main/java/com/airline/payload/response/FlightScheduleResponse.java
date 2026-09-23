package com.airline.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightScheduleResponse {

    private Long id;
    private Long flightId;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private Set<DayOfWeek> operatingDays;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;

    private Instant createdAt;
    private Instant updatedAt;

}