package com.airline.service;

import com.airline.enums.AircraftStatus;
import com.airline.payload.request.AircraftRequest;
import com.airline.payload.response.AircraftResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AircraftService {

    AircraftResponse createAircraft(AircraftRequest request, Long ownerId);

    AircraftResponse getAircraftById(Long id);

    AircraftResponse getAircraftByCode(String code);

    Page<AircraftResponse> getAllAircraft(Pageable pageable);

    Page<AircraftResponse> getAircraftByAirline(Long airlineId, Pageable pageable);

    List<AircraftResponse> getAircraftByAirlineAndStatus(Long airlineId, AircraftStatus status);

    AircraftResponse updateAircraft(Long aircraftId, AircraftRequest request, Long ownerId);

    AircraftResponse changeStatus(Long aircraftId, AircraftStatus status, Long requesterId);

    void deleteAircraft(Long aircraftId, Long requesterId);

}