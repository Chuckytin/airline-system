package com.airline.service.impl;

import com.airline.exception.ErrorCode;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.ValidationException;
import com.airline.mapper.FlightScheduleMapper;
import com.airline.model.Flight;
import com.airline.model.FlightSchedule;
import com.airline.payload.request.FlightScheduleRequest;
import com.airline.payload.response.FlightScheduleResponse;
import com.airline.repository.FlightRepository;
import com.airline.repository.FlightScheduleRepository;
import com.airline.service.FlightScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightScheduleServiceImpl implements FlightScheduleService {

    private final FlightScheduleRepository flightScheduleRepository;
    private final FlightRepository flightRepository;
    private final FlightScheduleMapper flightScheduleMapper;

    @Override
    @Transactional
    public FlightScheduleResponse createSchedule(FlightScheduleRequest request, Long requesterId) {
        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_NOT_FOUND, "Flight", request.getFlightId()
                ));

        validateTimes(request);
        validateDates(request);

        FlightSchedule schedule = flightScheduleMapper.toEntity(request);
        schedule.setFlight(flight);
        schedule.setActive(request.getActive() != null ? request.getActive() : true);

        FlightSchedule saved = flightScheduleRepository.save(schedule);

        FlightSchedule withDays = flightScheduleRepository.findByIdWithDetails(saved.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_SCHEDULE_NOT_FOUND, "FlightSchedule", saved.getId()
                ));

        return flightScheduleMapper.toResponse(withDays);
    }

    @Override
    public FlightScheduleResponse getScheduleById(Long id) {
        FlightSchedule schedule = flightScheduleRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_SCHEDULE_NOT_FOUND, "FlightSchedule", id
                ));
        return flightScheduleMapper.toResponse(schedule);
    }

    @Override
    public Page<FlightScheduleResponse> getAllSchedules(Pageable pageable) {
        return flightScheduleRepository.findAll(pageable)
                .map(flightScheduleMapper::toResponse);
    }

    @Override
    public List<FlightScheduleResponse> getSchedulesByFlightId(Long flightId) {
        if (!flightRepository.existsById(flightId)) {
            throw new ResourceNotFoundException(ErrorCode.FLIGHT_NOT_FOUND, "Flight", flightId);
        }
        return flightScheduleRepository.findByFlightIdWithDays(flightId)
                .stream()
                .map(flightScheduleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public FlightScheduleResponse updateSchedule(Long id, FlightScheduleRequest request, Long requesterId) {
        FlightSchedule existing = flightScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_SCHEDULE_NOT_FOUND, "FlightSchedule", id
                ));

        validateTimes(request);
        validateDates(request);

        if (request.getFlightId() != null && !request.getFlightId().equals(existing.getFlight().getId())) {
            Flight flight = flightRepository.findById(request.getFlightId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ErrorCode.FLIGHT_NOT_FOUND, "Flight", request.getFlightId()
                    ));
            existing.setFlight(flight);
        }

        flightScheduleMapper.updateEntity(existing, request);

        FlightSchedule updated = flightScheduleRepository.save(existing);

        FlightSchedule withDays = flightScheduleRepository.findByIdWithDetails(updated.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_SCHEDULE_NOT_FOUND, "FlightSchedule", updated.getId()
                ));

        return flightScheduleMapper.toResponse(withDays);
    }

    @Override
    @Transactional
    public FlightScheduleResponse changeActiveStatus(Long id, Boolean active, Long requesterId) {

        FlightSchedule schedule = flightScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_SCHEDULE_NOT_FOUND, "FlightSchedule", id
                ));

        schedule.setActive(active);
        FlightSchedule updated = flightScheduleRepository.save(schedule);

        FlightSchedule withDays = flightScheduleRepository.findByIdWithDetails(updated.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_SCHEDULE_NOT_FOUND, "FlightSchedule", updated.getId()
                ));

        return flightScheduleMapper.toResponse(withDays);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id, Long requesterId) {
        FlightSchedule schedule = flightScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.FLIGHT_SCHEDULE_NOT_FOUND, "FlightSchedule", id
                ));
        flightScheduleRepository.delete(schedule);
    }

    private void validateTimes(FlightScheduleRequest request) {
        if (request.getDepartureTime() != null && request.getArrivalTime() != null
                && !request.getArrivalTime().isAfter(request.getDepartureTime())) {
            throw new ValidationException(ErrorCode.INVALID_FLIGHT_TIMES,
                    "Arrival time must be after departure time");
        }
    }

    private void validateDates(FlightScheduleRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null
                && !request.getEndDate().isAfter(request.getStartDate())) {
            throw new ValidationException(ErrorCode.INVALID_FLIGHT_DATES,
                    "End date must be after start date");
        }
    }

}