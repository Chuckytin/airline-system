package com.airline.service;

import com.airline.payload.request.FlightScheduleRequest;
import com.airline.payload.response.FlightScheduleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FlightScheduleService {

    FlightScheduleResponse createSchedule(FlightScheduleRequest request, Long requesterId);

    FlightScheduleResponse getScheduleById(Long id);

    Page<FlightScheduleResponse> getAllSchedules(Pageable pageable);

    List<FlightScheduleResponse> getSchedulesByFlightId(Long flightId);

    FlightScheduleResponse updateSchedule(Long id, FlightScheduleRequest request, Long requesterId);

    FlightScheduleResponse changeActiveStatus(Long id, Boolean active, Long requesterId);

    void deleteSchedule(Long id, Long requesterId);

}