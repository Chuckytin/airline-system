package com.airline.service;

import com.airline.enums.SeatStatus;
import com.airline.enums.SeatType;
import com.airline.payload.request.SeatAssignmentRequest;
import com.airline.payload.request.SeatStatusUpdateRequest;
import com.airline.payload.response.SeatInstanceResponse;

import java.util.List;

public interface SeatInstanceService {

    SeatInstanceResponse getSeatInstanceById(Long id);

    List<SeatInstanceResponse> getSeatsByCabin(Long cabinId);

    List<SeatInstanceResponse> getSeatsByCabinAndStatus(Long cabinId, SeatStatus status);

    List<SeatInstanceResponse> getSeatsByCabinAndType(Long cabinId, SeatType type);

    SeatInstanceResponse changeStatus(Long id, SeatStatusUpdateRequest request);

    SeatInstanceResponse assign(Long id, SeatAssignmentRequest request);

    SeatInstanceResponse release(Long id);

}