package com.airline.service;

import com.airline.payload.request.FlightRequest;
import com.airline.payload.response.FlightResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FlightService {

    FlightResponse createFlight(FlightRequest request, Long requesterId);

    FlightResponse getFlightById(Long id);

    FlightResponse getFlightByFlightNumber(String flightNumber);

    Page<FlightResponse> getAllFlights(Pageable pageable);

    Page<FlightResponse> getFlightsByAirline(Long airlineId, Pageable pageable);

    FlightResponse updateFlight(Long id, FlightRequest request, Long requesterId);

    void deleteFlight(Long id, Long requesterId);

}