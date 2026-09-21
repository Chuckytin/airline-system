package com.airline.service.impl;

import com.airline.enums.AircraftStatus;
import com.airline.exception.ErrorCode;
import com.airline.exception.ResourceAlreadyExistsException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.ValidationException;
import com.airline.mapper.AircraftMapper;
import com.airline.model.Aircraft;
import com.airline.model.Airline;
import com.airline.payload.request.AircraftRequest;
import com.airline.payload.response.AircraftResponse;
import com.airline.repository.AircraftRepository;
import com.airline.repository.AirlineRepository;
import com.airline.service.AircraftMaintenanceService;
import com.airline.service.AircraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AircraftServiceImpl implements AircraftService {

    private final AircraftRepository aircraftRepository;
    private final AirlineRepository airlineRepository;
    private final AircraftMapper aircraftMapper;
    private final AircraftMaintenanceService maintenanceService;

    @Override
    @Transactional
    public AircraftResponse createAircraft(AircraftRequest request, Long ownerId) {
        if (aircraftRepository.existsByCode(request.getCode())) {
            throw new ResourceAlreadyExistsException(
                    ErrorCode.AIRCRAFT_CODE_ALREADY_EXISTS,
                    "Aircraft", "code", request.getCode()
            );
        }

        Airline airline = airlineRepository.findById(request.getAirlineId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.AIRLINE_NOT_FOUND, "Airline", request.getAirlineId()
                ));

        if (!airline.getOwnerId().equals(ownerId)) {
            throw new ValidationException(ErrorCode.ACCESS_DENIED,
                    "You don't have permission to add aircraft to this airline");
        }

        Aircraft aircraft = aircraftMapper.toEntity(request);
        aircraft.setAirline(airline);
        aircraft.setStatus(request.getStatus() != null ? request.getStatus() : AircraftStatus.ACTIVE);
        aircraft.setAvailable(request.getAvailable() != null ? request.getAvailable() : true);

        Aircraft saved = aircraftRepository.save(aircraft);
        return buildResponse(saved);
    }

    @Override
    public AircraftResponse getAircraftById(Long id) {
        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.AIRCRAFT_NOT_FOUND, "Aircraft", id
                ));
        return buildResponse(aircraft);
    }

    @Override
    public AircraftResponse getAircraftByCode(String code) {
        Aircraft aircraft = aircraftRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.AIRCRAFT_NOT_FOUND, "Aircraft not found with code: " + code
                ));
        return buildResponse(aircraft);
    }

    @Override
    public Page<AircraftResponse> getAllAircraft(Pageable pageable) {
        return aircraftRepository.findAll(pageable)
                .map(this::buildResponse);
    }

    @Override
    public Page<AircraftResponse> getAircraftByAirline(Long airlineId, Pageable pageable) {
        if (!airlineRepository.existsById(airlineId)) {
            throw new ResourceNotFoundException(ErrorCode.AIRLINE_NOT_FOUND, "Airline", airlineId);
        }
        return aircraftRepository.findAllByAirlineId(airlineId, pageable)
                .map(this::buildResponse);
    }

    @Override
    public List<AircraftResponse> getAircraftByAirlineAndStatus(Long airlineId, AircraftStatus status) {
        if (!airlineRepository.existsById(airlineId)) {
            throw new ResourceNotFoundException(ErrorCode.AIRLINE_NOT_FOUND, "Airline", airlineId);
        }
        return aircraftRepository.findAllByAirlineIdAndStatus(airlineId, status)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    @Transactional
    public AircraftResponse updateAircraft(Long aircraftId, AircraftRequest request, Long ownerId) {
        Aircraft existing = aircraftRepository.findById(aircraftId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.AIRCRAFT_NOT_FOUND, "Aircraft", aircraftId
                ));

        if (!existing.getAirline().getOwnerId().equals(ownerId)) {
            throw new ValidationException(ErrorCode.ACCESS_DENIED,
                    "You don't have permission to update this aircraft");
        }

        if (request.getCode() != null &&
                !request.getCode().equals(existing.getCode()) &&
                aircraftRepository.existsByCode(request.getCode())) {
            throw new ResourceAlreadyExistsException(
                    ErrorCode.AIRCRAFT_CODE_ALREADY_EXISTS,
                    "Aircraft", "code", request.getCode()
            );
        }

        if (request.getAirlineId() != null &&
                !request.getAirlineId().equals(existing.getAirline().getId())) {
            Airline airline = airlineRepository.findById(request.getAirlineId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ErrorCode.AIRLINE_NOT_FOUND, "Airline", request.getAirlineId()
                    ));
            existing.setAirline(airline);
        }

        aircraftMapper.updateEntity(existing, request);

        Aircraft updated = aircraftRepository.save(existing);
        return buildResponse(updated);
    }

    @Override
    @Transactional
    public AircraftResponse changeStatus(Long aircraftId, AircraftStatus newStatus, Long requesterId) {
        Aircraft aircraft = aircraftRepository.findById(aircraftId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.AIRCRAFT_NOT_FOUND, "Aircraft", aircraftId
                ));

        if (!aircraft.getAirline().getOwnerId().equals(requesterId)) {
            throw new ValidationException(ErrorCode.ACCESS_DENIED,
                    "You don't have permission to change the status of this aircraft");
        }

        AircraftStatus currentStatus = aircraft.getStatus();

        if (currentStatus == newStatus) {
            throw new ValidationException(ErrorCode.INVALID_AIRCRAFT_STATUS_TRANSITION,
                    "Aircraft is already in status " + newStatus);
        }

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new ValidationException(ErrorCode.INVALID_AIRCRAFT_STATUS_TRANSITION,
                    "Cannot transition from %s to %s".formatted(currentStatus, newStatus));
        }

        aircraft.setStatus(newStatus);
        Aircraft updated = aircraftRepository.save(aircraft);
        return buildResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAircraft(Long aircraftId, Long requesterId) {
        Aircraft aircraft = aircraftRepository.findById(aircraftId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.AIRCRAFT_NOT_FOUND, "Aircraft", aircraftId
                ));

        if (!aircraft.getAirline().getOwnerId().equals(requesterId)) {
            throw new ValidationException(ErrorCode.ACCESS_DENIED,
                    "You don't have permission to delete this aircraft");
        }

        aircraftRepository.delete(aircraft);
    }

    /**
     * Enriquece el response con campos calculados.
     */
    private AircraftResponse buildResponse(Aircraft aircraft) {
        AircraftResponse response = aircraftMapper.toResponse(aircraft);
        response.setTotalSeats(aircraft.getTotalSeats());
        response.setOperational(aircraft.isOperational());
        response.setRequiresMaintenance(maintenanceService.requiresMaintenance(aircraft));
        return response;
    }

    private boolean isValidStatusTransition(AircraftStatus from, AircraftStatus to) {
        return switch (from) {
            case ACTIVE -> to == AircraftStatus.MAINTENANCE
                    || to == AircraftStatus.INACTIVE
                    || to == AircraftStatus.RETIRED;
            case MAINTENANCE -> to == AircraftStatus.ACTIVE
                    || to == AircraftStatus.INACTIVE;
            case INACTIVE -> to == AircraftStatus.ACTIVE
                    || to == AircraftStatus.RETIRED;
            case RETIRED -> false;
        };
    }

}