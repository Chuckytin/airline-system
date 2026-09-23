package com.airline.service;

import com.airline.enums.FlightStatus;
import com.airline.payload.request.FlightInstanceRequest;
import com.airline.payload.response.FlightInstanceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface FlightInstanceService {

    FlightInstanceResponse createInstance(FlightInstanceRequest request, Long requesterId);

    FlightInstanceResponse getInstanceById(Long id);

    Page<FlightInstanceResponse> getAllInstances(Pageable pageable);

    Page<FlightInstanceResponse> getInstancesByFlightId(Long flightId, Pageable pageable);

    Page<FlightInstanceResponse> getInstancesByDate(LocalDate flightDate, Pageable pageable);

    FlightInstanceResponse updateInstance(Long id, FlightInstanceRequest request, Long requesterId);

    FlightInstanceResponse changeStatus(Long id, FlightStatus newStatus, Long requesterId);

    FlightInstanceResponse changeActiveStatus(Long id, Boolean active, Long requesterId);

    void deleteInstance(Long id, Long requesterId);

}