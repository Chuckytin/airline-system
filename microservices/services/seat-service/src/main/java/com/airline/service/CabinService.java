package com.airline.service;

import com.airline.payload.request.CabinRequest;
import com.airline.payload.response.CabinAvailabilityResponse;
import com.airline.payload.response.CabinResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CabinService {

    CabinResponse createCabin(CabinRequest request, Long requesterId);

    CabinResponse getCabinById(Long id);

    CabinResponse getCabinWithDetails(Long id);

    List<CabinResponse> getCabinsByFlightInstance(Long flightInstanceId);

    Page<CabinResponse> getAllCabins(Pageable pageable);

    CabinAvailabilityResponse getAvailability(Long cabinId);

    CabinResponse updateCabin(Long id, CabinRequest request, Long requesterId);

    CabinResponse changeActiveStatus(Long id, Boolean active, Long requesterId);

    void deleteCabin(Long id, Long requesterId);

}