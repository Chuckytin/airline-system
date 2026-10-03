package com.airline.service;

import com.airline.payload.request.SeatMapFlagsRequest;
import com.airline.payload.response.SeatMapResponse;

import java.util.List;

public interface SeatMapService {

    SeatMapResponse getSeatMapById(Long id);

    List<SeatMapResponse> getSeatMapsByCabin(Long cabinId);

    SeatMapResponse updateFlags(Long id, SeatMapFlagsRequest request);

}