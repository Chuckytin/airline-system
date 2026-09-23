package com.airline.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightRequest {

    @NotBlank(message = "Flight number is mandatory")
    @Size(max = 10, message = "Flight number cannot exceed 10 characters")
    private String flightNumber;

    @NotNull(message = "Airline ID is mandatory")
    private Long airlineId;

    @NotNull(message = "Departure airport ID is mandatory")
    private Long departureAirportId;

    @NotNull(message = "Arrival airport ID is mandatory")
    private Long arrivalAirportId;

    @Min(value = 1, message = "Estimated duration must be positive")
    private Integer estimatedDurationMinutes;

    @Min(value = 1, message = "Distance must be positive")
    private Integer distanceKm;

}