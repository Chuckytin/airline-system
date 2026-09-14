package com.airline.payload.request;

import com.airline.embeddable.Address;
import com.airline.embeddable.GeoCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirportRequest {

    @NotBlank(message = "IATA code is mandatory")
    @Size(min = 3, max = 3, message = "IATA code must be exactly 3 characters")
    private String iataCode;

    @NotBlank(message = "Airport name is mandatory")
    private String name;

    @Valid
    private Address address;

    @Valid
    private GeoCode geoCode;

    @Pattern(
            regexp = "^[A-Za-z]+/[A-Za-z_]+$",
            message = "TimeZone must be in format Area/Location (e.g., Europe/Madrid)"
    )
    private String timeZone;

    @NotNull(message = "City ID is mandatory")
    private Long cityId;

}
