package com.airline.payload.response;

import com.airline.embeddable.Address;
import com.airline.embeddable.GeoCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirportResponse {

    private Long id;
    private String iataCode;
    private String name;
    private String detailedName;
    private Address address;
    private GeoCode geoCode;
    private String timeZone;
    private CityResponse city;

    public String getDetailedName() {
        if (name == null) {
            return null;
        }
        if (city != null && city.getCountryCode() != null) {
            return name.toUpperCase() + "/" + city.getCountryCode();
        }
        return name.toUpperCase();
    }

}
