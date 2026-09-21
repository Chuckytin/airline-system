package com.airline.payload.request;

import com.airline.enums.AircraftStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AircraftRequest {

    @NotBlank(message = "Aircraft code is mandatory")
    @Size(max = 20, message = "Aircraft code cannot exceed 20 characters")
    private String code;

    @NotBlank(message = "Model is mandatory")
    @Size(max = 100, message = "Model cannot exceed 100 characters")
    private String model;

    @NotBlank(message = "Manufacturer is mandatory")
    @Size(max = 50, message = "Manufacturer cannot exceed 50 characters")
    private String manufacturer;

    @PositiveOrZero(message = "Economy seats cannot be negative")
    private Integer economySeats;

    @PositiveOrZero(message = "Premium economy seats cannot be negative")
    private Integer premiumEconomySeats;

    @PositiveOrZero(message = "Business seats cannot be negative")
    private Integer businessSeats;

    @PositiveOrZero(message = "First class seats cannot be negative")
    private Integer firstClassSeats;

    @Positive(message = "Range must be positive")
    private Integer rangeKm;

    @Min(value = 0, message = "Cruising speed cannot be negative")
    private Integer cruisingSpeedKmh;

    @Positive(message = "Maximum altitude must be positive")
    private Integer maxAltitudeFt;

    @Min(value = 1900, message = "Year of manufacture must be valid")
    private Integer yearOfManufacture;

    private LocalDate registrationDate;

    private LocalDate nextMaintenanceDate;

    private AircraftStatus status;

    private Boolean available;

    @NotNull(message = "Airline ID is mandatory")
    private Long airlineId;

    private Long currentAirportId;

    /**
     * Valida que la aeronave tenga al menos un asiento.
     */
    @AssertTrue(message = "Aircraft must have at least one seat")
    private boolean isAtLeastOneSeat() {
        return (economySeats != null && economySeats > 0)
                || (premiumEconomySeats != null && premiumEconomySeats > 0)
                || (businessSeats != null && businessSeats > 0)
                || (firstClassSeats != null && firstClassSeats > 0);
    }

}