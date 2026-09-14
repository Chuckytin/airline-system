package com.airline.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CityRequest {

    @NotBlank(message = "City name is required")
    @Size(max = 100, message = "City name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "City code is required")
    @Size(min = 3, max = 3, message = "City code must be exactly 3 characters")
    private String cityCode;

    @NotBlank(message = "Country code is required")
    @Size(min = 2, max = 3, message = "Country code must be between 2 and 3 characters")
    private String countryCode;

    @NotBlank(message = "Country name is required")
    @Size(max = 100, message = "Country name cannot exceed 100 characters")
    private String countryName;

    @Size(max = 10, message = "Region code cannot exceed 10 characters")
    private String regionCode;

    @Size(max = 10)
    @Pattern(
            regexp = "^[+-]\\d{2}:\\d{2}$",
            message = "TimeZoneOffset must be in format Offset (e.g., +02:00, -05:00)"
    )
    private String timeZoneOffset;
}
