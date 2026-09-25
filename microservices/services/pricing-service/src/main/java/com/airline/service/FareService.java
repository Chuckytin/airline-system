package com.airline.service;

import com.airline.payload.request.FareRequest;
import com.airline.payload.response.FareResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FareService {

    FareResponse createFare(FareRequest request, Long requesterId);

    FareResponse getFareById(Long id);

    List<FareResponse> getFaresByFlightId(Long flightId);

    Page<FareResponse> getAllFares(Pageable pageable);

    FareResponse updateFare(Long id, FareRequest request, Long requesterId);

    FareResponse changeActiveStatus(Long id, Boolean active, Long requesterId);

    void deleteFare(Long id, Long requesterId);

}