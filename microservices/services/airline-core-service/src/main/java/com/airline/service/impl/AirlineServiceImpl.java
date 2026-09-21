package com.airline.service.impl;

import com.airline.enums.AirlineStatus;
import com.airline.exception.ErrorCode;
import com.airline.exception.ResourceAlreadyExistsException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.ValidationException;
import com.airline.mapper.AirlineMapper;
import com.airline.model.Airline;
import com.airline.payload.request.AirlineRequest;
import com.airline.payload.response.AirlineDropdownItem;
import com.airline.payload.response.AirlineResponse;
import com.airline.repository.AirlineRepository;
import com.airline.service.AirlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AirlineServiceImpl implements AirlineService {

    private final AirlineRepository airlineRepository;
    private final AirlineMapper airlineMapper;

    @Override
    @Transactional
    public AirlineResponse createAirline(AirlineRequest request, Long ownerId) {

        if (airlineRepository.existsByIataCode(request.getIataCode())) {
            throw new ResourceAlreadyExistsException(
                    ErrorCode.AIRLINE_IATA_ALREADY_EXISTS,
                    "Airline", "iataCode", request.getIataCode()
            );
        }

        if (airlineRepository.existsByIcaoCode(request.getIcaoCode())) {
            throw new ResourceAlreadyExistsException(
                    ErrorCode.AIRLINE_ICAO_ALREADY_EXISTS,
                    "Airline", "icaoCode", request.getIcaoCode()
            );
        }

        if (airlineRepository.existsByOwnerId(ownerId)) {
            throw new ResourceAlreadyExistsException(
                    ErrorCode.AIRLINE_OWNER_ALREADY_HAS_AIRLINE,
                    "Airline", "ownerId", ownerId
            );
        }

        Airline airline = airlineMapper.toEntity(request);
        airline.setOwnerId(ownerId);

        Airline saved = airlineRepository.save(airline);
        return airlineMapper.toResponse(saved);
    }


    @Override
    public AirlineResponse getAirlineById(Long id) {

        Airline airline = airlineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.AIRLINE_NOT_FOUND, "Airline", id
                ));
        return airlineMapper.toResponse(airline);
    }

    @Override
    public AirlineResponse getAirlineByOwnerId(Long ownerId) {

        Airline airline = airlineRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.AIRLINE_NOT_FOUND, "Airline not found for owner: " + ownerId
                ));
        return airlineMapper.toResponse(airline);
    }

    @Override
    public Page<AirlineResponse> getAllAirlines(Pageable pageable) {
        return airlineRepository.findAll(pageable)
                .map(airlineMapper::toResponse);
    }

    @Override
    public List<AirlineDropdownItem> getAirlinesForDropdown() {
        return airlineRepository.findAll().stream()
                .map(airlineMapper::toDropdownItem)
                .toList();
    }

    @Override
    @Transactional
    public AirlineResponse updateAirline(Long airlineId, AirlineRequest request, Long requesterId) {

        Airline existing = airlineRepository.findById(airlineId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.AIRLINE_NOT_FOUND, "Airline", airlineId
                ));

        if (!existing.getOwnerId().equals(requesterId)) {
            throw new ValidationException(ErrorCode.ACCESS_DENIED,
                    "You don't have permission to update this airline");
        }

        if (request.getIataCode() != null &&
                !request.getIataCode().equals(existing.getIataCode()) &&
                airlineRepository.existsByIataCode(request.getIataCode())) {
            throw new ResourceAlreadyExistsException(
                    ErrorCode.AIRLINE_IATA_ALREADY_EXISTS,
                    "Airline", "iataCode", request.getIataCode()
            );
        }

        if (request.getIcaoCode() != null &&
                !request.getIcaoCode().equals(existing.getIcaoCode()) &&
                airlineRepository.existsByIcaoCode(request.getIcaoCode())) {
            throw new ResourceAlreadyExistsException(
                    ErrorCode.AIRLINE_ICAO_ALREADY_EXISTS,
                    "Airline", "icaoCode", request.getIcaoCode()
            );
        }

        airlineMapper.updateEntity(existing, request);

        existing.setUpdatedById(requesterId);

        Airline updated = airlineRepository.save(existing);

        return airlineMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public AirlineResponse changeStatusByAdmin(Long airlineId, AirlineStatus newStatus, Long requesterId) {
        Airline airline = airlineRepository.findById(airlineId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.AIRLINE_NOT_FOUND, "Airline", airlineId
                ));

        AirlineStatus currentStatus = airline.getStatus();

        if (currentStatus == newStatus) {
            throw new ValidationException(ErrorCode.INVALID_AIRLINE_STATUS_TRANSITION,
                    "Airline is already in status " + newStatus);
        }

        if (!isValidTransition(currentStatus, newStatus)) {
            throw new ValidationException(ErrorCode.INVALID_AIRLINE_STATUS_TRANSITION,
                    "Cannot transition from %s to %s".formatted(currentStatus, newStatus));
        }

        airline.setStatus(newStatus);
        Airline updated = airlineRepository.save(airline);
        return airlineMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAirline(Long airlineId, Long ownerId) {
        Airline airline = airlineRepository.findById(airlineId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.AIRLINE_NOT_FOUND, "Airline", airlineId
                ));

        if (!airline.getOwnerId().equals(ownerId)) {
            throw new ValidationException(ErrorCode.ACCESS_DENIED,
                    "You don't have permission to delete this airline");
        }

        airlineRepository.delete(airline);
    }

    private boolean isValidTransition(AirlineStatus from, AirlineStatus to) {
        return switch (from) {
            case INACTIVE -> to == AirlineStatus.ACTIVE || to == AirlineStatus.BANNED;
            case ACTIVE -> to == AirlineStatus.INACTIVE || to == AirlineStatus.BANNED;
            case BANNED -> to == AirlineStatus.INACTIVE;
        };
    }

}