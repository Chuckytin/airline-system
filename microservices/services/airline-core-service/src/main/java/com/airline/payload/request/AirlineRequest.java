package com.airline.payload.request;

import com.airline.embeddable.Support;
import com.airline.enums.AirlineStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirlineRequest {

    @NotBlank(message = "IATA code is mandatory")
    @Size(min = 2, max = 3, message = "IATA code must be between 2 and 3 characters")
    private String iataCode;

    @NotBlank(message = "ICAO code is mandatory")
    @Size(min = 3, max = 4, message = "ICAO code must be between 3 and 4 characters")
    private String icaoCode;

    @NotBlank(message = "Airline name is mandatory")
    @Size(max = 100, message = "Airline name cannot exceed 100 characters")
    private String name;

    @Size(max = 100, message = "Alias cannot exceed 100 characters")
    private String alias;

    @Size(max = 255, message = "Logo URL cannot exceed 255 characters")
    private String logoUrl;

    @Size(max = 255, message = "Website cannot exceed 255 characters")
    private String website;

    private AirlineStatus status;

    @Size(max = 50, message = "Alliance cannot exceed 50 characters")
    private String alliance;

    private Long headquartersCityId;

    @Valid
    private Support support;

}