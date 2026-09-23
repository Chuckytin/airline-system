package com.airline.payload.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightScheduleRequest {

    @NotNull(message = "Flight ID is mandatory")
    private Long flightId;

    @NotNull(message = "Departure time is mandatory")
    private LocalTime departureTime;

    @NotNull(message = "Arrival time is mandatory")
    private LocalTime arrivalTime;

    @NotEmpty(message = "At least one operating day is mandatory")
    private Set<DayOfWeek> operatingDays;

    @NotNull(message = "Start date is mandatory")
    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean active;

}