package com.airline.payload.response;

import com.airline.embeddable.Support;
import com.airline.enums.AirlineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AirlineResponse {

    private Long id;
    private String iataCode;
    private String icaoCode;
    private String name;
    private String alias;
    private String logoUrl;
    private String website;
    private AirlineStatus status;
    private String alliance;
    private Support support;
    private Instant createdAt;
    private Instant updatedAt;

    private Long ownerId;
    private Long updatedById;
    private Long headquartersCityId;
}
