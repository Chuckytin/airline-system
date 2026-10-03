package com.airline.service.impl;

import com.airline.exception.ErrorCode;
import com.airline.exception.ResourceNotFoundException;
import com.airline.mapper.SeatMapMapper;
import com.airline.model.SeatMap;
import com.airline.payload.request.SeatMapFlagsRequest;
import com.airline.payload.response.SeatMapResponse;
import com.airline.repository.SeatMapRepository;
import com.airline.service.SeatMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatMapServiceImpl implements SeatMapService {

    private final SeatMapRepository seatMapRepository;
    private final SeatMapMapper seatMapMapper;

    @Override
    public SeatMapResponse getSeatMapById(Long id) {
        SeatMap seatMap = seatMapRepository.findByIdWithCabin(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.SEAT_MAP_NOT_FOUND, "SeatMap", id
                ));
        return seatMapMapper.toResponse(seatMap);
    }

    @Override
    public List<SeatMapResponse> getSeatMapsByCabin(Long cabinId) {
        return seatMapRepository.findByCabinIdWithCabin(cabinId)
                .stream()
                .map(seatMapMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SeatMapResponse updateFlags(Long id, SeatMapFlagsRequest request) {
        SeatMap seatMap = seatMapRepository.findByIdWithCabin(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.SEAT_MAP_NOT_FOUND, "SeatMap", id
                ));

        if (request.getHasExtraLegroom() != null) {
            seatMap.setHasExtraLegroom(request.getHasExtraLegroom());
        }
        if (request.getIsExitRow() != null) {
            seatMap.setIsExitRow(request.getIsExitRow());
        }

        SeatMap updated = seatMapRepository.save(seatMap);
        return seatMapMapper.toResponse(updated);
    }

}