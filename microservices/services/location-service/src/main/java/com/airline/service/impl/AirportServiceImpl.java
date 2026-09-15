package com.airline.service.impl;

import com.airline.exception.ErrorCode;
import com.airline.exception.ResourceAlreadyExistsException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.mapper.AirportMapper;
import com.airline.model.Airport;
import com.airline.model.City;
import com.airline.payload.request.AirportRequest;
import com.airline.payload.response.AirportResponse;
import com.airline.repository.AirportRepository;
import com.airline.repository.CityRepository;
import com.airline.service.AirportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AirportServiceImpl implements AirportService {

    private final AirportRepository airportRepository;
    private final CityRepository cityRepository;
    private final AirportMapper airportMapper;

    @Override
    @Transactional
    public AirportResponse createAirport(AirportRequest airportRequest) {
        if (airportRepository.existsByIataCode(airportRequest.getIataCode())) {
            throw new ResourceAlreadyExistsException(
                    ErrorCode.AIRPORT_IATA_ALREADY_EXISTS,
                    "Airport", "iataCode", airportRequest.getIataCode()
            );
        }

        City city = cityRepository.findById(airportRequest.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CITY_NOT_FOUND, "City", airportRequest.getCityId()
                ));

        Airport airport = airportMapper.toEntity(airportRequest);
        airport.setCity(city);

        Airport savedAirport = airportRepository.save(airport);
        return airportMapper.toResponse(savedAirport);
    }

    @Override
    public AirportResponse getAirportById(Long id) {
        Airport airport = airportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.AIRPORT_NOT_FOUND, "Airport", id
                ));
        return airportMapper.toResponse(airport);
    }

    @Override
    public Page<AirportResponse> getAllAirports(Pageable pageable) {
        return airportRepository.findAll(pageable)
                .map(airportMapper::toResponse);
    }

    @Override
    @Transactional
    public AirportResponse updateAirport(Long id, AirportRequest airportRequest) {
        Airport existingAirport = airportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.AIRPORT_NOT_FOUND, "Airport", id
                ));

        // Verificar IATA duplicado solo si cambia
        if (airportRequest.getIataCode() != null &&
                !airportRequest.getIataCode().equals(existingAirport.getIataCode()) &&
                airportRepository.existsByIataCode(airportRequest.getIataCode())) {
            throw new ResourceAlreadyExistsException(
                    ErrorCode.AIRPORT_IATA_ALREADY_EXISTS,
                    "Airport", "iataCode", airportRequest.getIataCode()
            );
        }

        // Si cambia el cityId, actualizar la relación
        if (airportRequest.getCityId() != null &&
                !airportRequest.getCityId().equals(existingAirport.getCity().getId())) {
            City city = cityRepository.findById(airportRequest.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ErrorCode.CITY_NOT_FOUND, "City", airportRequest.getCityId()
                    ));
            existingAirport.setCity(city);
        }

        airportMapper.updateEntity(existingAirport, airportRequest);
        Airport updatedAirport = airportRepository.save(existingAirport);
        return airportMapper.toResponse(updatedAirport);
    }

    @Override
    @Transactional
    public void deleteAirportById(Long id) {
        if (!airportRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCode.AIRPORT_NOT_FOUND, "Airport", id);
        }
        airportRepository.deleteById(id);
    }

    @Override
    public Page<AirportResponse> getAirportsByCityId(Long cityId, Pageable pageable) {
        if (!cityRepository.existsById(cityId)) {
            throw new ResourceNotFoundException(ErrorCode.CITY_NOT_FOUND, "City", cityId);
        }
        return airportRepository.findByCityId(cityId, pageable)
                .map(airportMapper::toResponse);
    }

}