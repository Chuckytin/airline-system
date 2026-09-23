package com.airline.service.impl;

import com.airline.exception.ErrorCode;
import com.airline.exception.ResourceAlreadyExistsException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.ValidationException;
import com.airline.mapper.FlightMapper;
import com.airline.model.Flight;
import com.airline.payload.request.FlightRequest;
import com.airline.payload.response.FlightResponse;
import com.airline.repository.FlightRepository;
import com.airline.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;

    @Override
    @Transactional
    public FlightResponse createFlight(FlightRequest request, Long requesterId) {

        if (request.getDepartureAirportId().equals(request.getArrivalAirportId())) {
            throw new ValidationException(ErrorCode.INVALID_FLIGHT_ROUTE,
                    "Departure and arrival airports must be different");
        }

        if (flightRepository.existsByAirlineIdAndFlightNumber(
                request.getAirlineId(), request.getFlightNumber())) {
            throw new ResourceAlreadyExistsException(
                    ErrorCode.FLIGHT_NUMBER_ALREADY_EXISTS,
                    "Flight", "flightNumber", request.getFlightNumber()
            );
        }

        Flight flight = flightMapper.toEntity(request);

        Flight saved = flightRepository.save(flight);
        return flightMapper.toResponse(saved);
    }

    @Override
    public FlightResponse getFlightById(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_NOT_FOUND, "Flight", id
                ));
        return flightMapper.toResponse(flight);
    }

    @Override
    public FlightResponse getFlightByFlightNumber(String flightNumber) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_NOT_FOUND,
                        "Flight not found with number: " + flightNumber
                ));
        return flightMapper.toResponse(flight);
    }

    @Override
    public Page<FlightResponse> getAllFlights(Pageable pageable) {
        return flightRepository.findAll(pageable)
                .map(flightMapper::toResponse);
    }

    @Override
    public Page<FlightResponse> getFlightsByAirline(Long airlineId, Pageable pageable) {
        return flightRepository.findByAirlineId(airlineId, pageable)
                .map(flightMapper::toResponse);
    }

    @Override
    @Transactional
    public FlightResponse updateFlight(Long id, FlightRequest request, Long requesterId) {
        Flight existing = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_NOT_FOUND, "Flight", id
                ));

        if (request.getDepartureAirportId() != null &&
                request.getArrivalAirportId() != null &&
                request.getDepartureAirportId().equals(request.getArrivalAirportId())) {
            throw new ValidationException(ErrorCode.INVALID_FLIGHT_ROUTE,
                    "Departure and arrival airports must be different");
        }

        if (request.getFlightNumber() != null &&
                !request.getFlightNumber().equals(existing.getFlightNumber())) {
            Long airlineId = request.getAirlineId() != null ? request.getAirlineId() : existing.getAirlineId();
            if (flightRepository.existsByAirlineIdAndFlightNumber(airlineId, request.getFlightNumber())) {
                throw new ResourceAlreadyExistsException(
                        ErrorCode.FLIGHT_NUMBER_ALREADY_EXISTS,
                        "Flight", "flightNumber", request.getFlightNumber()
                );
            }
        }

        flightMapper.updateEntity(existing, request);

        Flight updated = flightRepository.save(existing);
        return flightMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteFlight(Long id, Long requesterId) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_NOT_FOUND, "Flight", id
                ));
        flightRepository.delete(flight);
    }

}