package com.airline.payload.request;

import com.airline.enums.FlightStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightInstanceRequest {

    @NotNull(message = "Flight ID is mandatory")
    private Long flightId;

    private Long scheduleId;

    @NotNull(message = "Aircraft ID is mandatory")
    private Long aircraftId;

    @NotNull(message = "Flight date is mandatory")
    private LocalDate flightDate;

    @NotNull(message = "Departure date time is mandatory")
    private LocalDateTime departureDateTime;

    @NotNull(message = "Arrival date time is mandatory")
    private LocalDateTime arrivalDateTime;

    @NotNull(message = "Total seats is mandatory")
    @Min(value = 1, message = "Total seats must be at least 1")
    private Integer totalSeats;

    @NotNull(message = "Available seats is mandatory")
    @Min(value = 0, message = "Available seats cannot be negative")
    private Integer availableSeats;

    private FlightStatus status;

    @Min(value = 0, message = "Min advance booking days cannot be negative")
    private Integer minAdvanceBookingDays;

    @Min(value = 0, message = "Max advance booking days cannot be negative")
    private Integer maxAdvanceBookingDays;

    private Boolean active;

}