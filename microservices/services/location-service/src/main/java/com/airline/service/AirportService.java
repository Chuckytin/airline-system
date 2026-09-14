package com.airline.service;

import com.airline.payload.request.AirportRequest;
import com.airline.payload.response.AirportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AirportService {

    AirportResponse createAirport(AirportRequest airportRequest);

    AirportResponse getAirportById(Long id);

    Page<AirportResponse> getAllAirports(Pageable pageable);

    AirportResponse updateAirport(Long id, AirportRequest airportRequest);

    void deleteAirportById(Long id);

    Page<AirportResponse> getAirportsByCityId(Long cityId, Pageable pageable);

}
