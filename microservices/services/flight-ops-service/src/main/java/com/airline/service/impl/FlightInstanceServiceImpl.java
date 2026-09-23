package com.airline.service.impl;

import com.airline.enums.FlightStatus;
import com.airline.exception.ErrorCode;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.ValidationException;
import com.airline.mapper.FlightInstanceMapper;
import com.airline.model.Flight;
import com.airline.model.FlightInstance;
import com.airline.model.FlightSchedule;
import com.airline.payload.request.FlightInstanceRequest;
import com.airline.payload.response.FlightInstanceResponse;
import com.airline.repository.FlightInstanceRepository;
import com.airline.repository.FlightRepository;
import com.airline.repository.FlightScheduleRepository;
import com.airline.service.FlightInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightInstanceServiceImpl implements FlightInstanceService {

    private final FlightInstanceRepository flightInstanceRepository;
    private final FlightRepository flightRepository;
    private final FlightScheduleRepository flightScheduleRepository;
    private final FlightInstanceMapper flightInstanceMapper;

    @Override
    @Transactional
    public FlightInstanceResponse createInstance(FlightInstanceRequest request, Long requesterId) {
        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_NOT_FOUND, "Flight", request.getFlightId()
                ));

        FlightSchedule schedule = null;
        if (request.getScheduleId() != null) {
            schedule = flightScheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ErrorCode.FLIGHT_SCHEDULE_NOT_FOUND, "FlightSchedule", request.getScheduleId()
                    ));
        }

        validateDateTime(request);
        validateSeats(request);

        FlightInstance instance = flightInstanceMapper.toEntity(request);
        instance.setFlight(flight);
        instance.setSchedule(schedule);
        instance.setStatus(request.getStatus() != null ? request.getStatus() : FlightStatus.SCHEDULED);
        instance.setActive(request.getActive() != null ? request.getActive() : true);

        FlightInstance saved = flightInstanceRepository.save(instance);

        FlightInstance withDetails = flightInstanceRepository.findByIdWithDetails(saved.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_INSTANCE_NOT_FOUND, "FlightInstance", saved.getId()
                ));

        return buildResponse(withDetails);
    }

    @Override
    public FlightInstanceResponse getInstanceById(Long id) {
        FlightInstance instance = flightInstanceRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_INSTANCE_NOT_FOUND, "FlightInstance", id
                ));
        return buildResponse(instance);
    }

    @Override
    public Page<FlightInstanceResponse> getAllInstances(Pageable pageable) {
        return flightInstanceRepository.findAll(pageable)
                .map(this::buildResponse);
    }

    @Override
    public Page<FlightInstanceResponse> getInstancesByFlightId(Long flightId, Pageable pageable) {
        if (!flightRepository.existsById(flightId)) {
            throw new ResourceNotFoundException(ErrorCode.FLIGHT_NOT_FOUND, "Flight", flightId);
        }
        return flightInstanceRepository.findByFlightId(flightId, pageable)
                .map(this::buildResponse);
    }

    @Override
    public Page<FlightInstanceResponse> getInstancesByDate(LocalDate flightDate, Pageable pageable) {
        return flightInstanceRepository.findByFlightDate(flightDate, pageable)
                .map(this::buildResponse);
    }

    @Override
    @Transactional
    public FlightInstanceResponse updateInstance(Long id, FlightInstanceRequest request, Long requesterId) {
        FlightInstance existing = flightInstanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_INSTANCE_NOT_FOUND, "FlightInstance", id
                ));

        validateDateTime(request);
        validateSeats(request);

        if (request.getScheduleId() != null &&
                (existing.getSchedule() == null ||
                        !request.getScheduleId().equals(existing.getSchedule().getId()))) {
            FlightSchedule schedule = flightScheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ErrorCode.FLIGHT_SCHEDULE_NOT_FOUND, "FlightSchedule", request.getScheduleId()
                    ));
            existing.setSchedule(schedule);
        }

        flightInstanceMapper.updateEntity(existing, request);

        FlightInstance updated = flightInstanceRepository.save(existing);

        FlightInstance withDetails = flightInstanceRepository.findByIdWithDetails(updated.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_INSTANCE_NOT_FOUND, "FlightInstance", updated.getId()
                ));

        return buildResponse(withDetails);
    }

    @Override
    @Transactional
    public FlightInstanceResponse changeStatus(Long id, FlightStatus newStatus, Long requesterId) {
        FlightInstance instance = flightInstanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_INSTANCE_NOT_FOUND, "FlightInstance", id
                ));

        if (instance.getStatus() == newStatus) {
            throw new ValidationException(ErrorCode.INVALID_FLIGHT_STATUS_TRANSITION,
                    "FlightInstance is already in status " + newStatus);
        }

        if (!isValidStatusTransition(instance.getStatus(), newStatus)) {
            throw new ValidationException(ErrorCode.INVALID_FLIGHT_STATUS_TRANSITION,
                    "Cannot transition from %s to %s".formatted(instance.getStatus(), newStatus));
        }

        instance.setStatus(newStatus);
        FlightInstance updated = flightInstanceRepository.save(instance);

        FlightInstance withDetails = flightInstanceRepository.findByIdWithDetails(updated.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_INSTANCE_NOT_FOUND, "FlightInstance", updated.getId()
                ));

        return buildResponse(withDetails);
    }

    @Override
    @Transactional
    public FlightInstanceResponse changeActiveStatus(Long id, Boolean active, Long requesterId) {
        FlightInstance instance = flightInstanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_INSTANCE_NOT_FOUND, "FlightInstance", id
                ));

        instance.setActive(active);
        FlightInstance updated = flightInstanceRepository.save(instance);

        FlightInstance withDetails = flightInstanceRepository.findByIdWithDetails(updated.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_INSTANCE_NOT_FOUND, "FlightInstance", updated.getId()
                ));

        return buildResponse(withDetails);
    }

    @Override
    @Transactional
    public void deleteInstance(Long id, Long requesterId) {
        FlightInstance instance = flightInstanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_INSTANCE_NOT_FOUND, "FlightInstance", id
                ));
        flightInstanceRepository.delete(instance);
    }

    /**
     * Convierte la entidad a response y calcula formattedDuration.
     */
    private FlightInstanceResponse buildResponse(FlightInstance instance) {
        FlightInstanceResponse response = flightInstanceMapper.toResponse(instance);
        response.setFormattedDuration(instance.getFormattedDuration());
        return response;
    }

    private void validateDateTime(FlightInstanceRequest request) {
        if (request.getArrivalDateTime() != null && request.getDepartureDateTime() != null
                && !request.getArrivalDateTime().isAfter(request.getDepartureDateTime())) {
            throw new ValidationException(ErrorCode.INVALID_FLIGHT_TIMES,
                    "Arrival date time must be after departure date time");
        }
    }

    private void validateSeats(FlightInstanceRequest request) {
        if (request.getTotalSeats() != null && request.getAvailableSeats() != null
                && request.getAvailableSeats() > request.getTotalSeats()) {
            throw new ValidationException(ErrorCode.INVALID_FLIGHT_SEATS,
                    "Available seats cannot exceed total seats");
        }
    }

    private boolean isValidStatusTransition(FlightStatus from, FlightStatus to) {
        return switch (from) {
            case SCHEDULED -> to == FlightStatus.BOARDING
                    || to == FlightStatus.DELAYED
                    || to == FlightStatus.CANCELLED;
            case BOARDING -> to == FlightStatus.DEPARTED
                    || to == FlightStatus.DELAYED
                    || to == FlightStatus.CANCELLED;
            case DEPARTED -> to == FlightStatus.IN_AIR
                    || to == FlightStatus.DIVERTED;
            case IN_AIR -> to == FlightStatus.LANDED
                    || to == FlightStatus.DIVERTED;
            case LANDED -> to == FlightStatus.ARRIVED
                    || to == FlightStatus.DIVERTED;
            case ARRIVED -> to == FlightStatus.COMPLETED;
            case DELAYED -> to == FlightStatus.SCHEDULED
                    || to == FlightStatus.BOARDING
                    || to == FlightStatus.CANCELLED;
            case DIVERTED -> to == FlightStatus.LANDED;
            case CANCELLED, COMPLETED -> false;
        };
    }

}