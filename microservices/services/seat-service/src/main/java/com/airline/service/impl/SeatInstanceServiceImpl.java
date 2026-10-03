package com.airline.service.impl;

import com.airline.enums.SeatStatus;
import com.airline.enums.SeatType;
import com.airline.exception.ErrorCode;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.ValidationException;
import com.airline.mapper.SeatInstanceMapper;
import com.airline.model.Cabin;
import com.airline.model.SeatInstance;
import com.airline.payload.request.SeatAssignmentRequest;
import com.airline.payload.request.SeatStatusUpdateRequest;
import com.airline.payload.response.SeatInstanceResponse;
import com.airline.repository.CabinRepository;
import com.airline.repository.SeatInstanceRepository;
import com.airline.service.SeatInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatInstanceServiceImpl implements SeatInstanceService {

    private final SeatInstanceRepository seatInstanceRepository;
    private final CabinRepository cabinRepository;
    private final SeatInstanceMapper seatInstanceMapper;

    @Override
    public SeatInstanceResponse getSeatInstanceById(Long id) {
        SeatInstance seat = seatInstanceRepository.findByIdWithCabin(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.SEAT_INSTANCE_NOT_FOUND, "SeatInstance", id
                ));
        return seatInstanceMapper.toResponse(seat);
    }

    @Override
    public List<SeatInstanceResponse> getSeatsByCabin(Long cabinId) {
        assertCabinExists(cabinId);
        return seatInstanceRepository.findByCabinIdOrderBySeatRowAscColumnLetterAsc(cabinId)
                .stream()
                .map(seatInstanceMapper::toResponse)
                .toList();
    }

    @Override
    public List<SeatInstanceResponse> getSeatsByCabinAndStatus(Long cabinId, SeatStatus status) {
        assertCabinExists(cabinId);
        return seatInstanceRepository.findByCabinIdAndStatus(cabinId, status)
                .stream()
                .map(seatInstanceMapper::toResponse)
                .toList();
    }

    @Override
    public List<SeatInstanceResponse> getSeatsByCabinAndType(Long cabinId, SeatType type) {
        assertCabinExists(cabinId);
        return seatInstanceRepository.findByCabinIdAndSeatType(cabinId, type)
                .stream()
                .map(seatInstanceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SeatInstanceResponse changeStatus(Long id, SeatStatusUpdateRequest request) {
        SeatInstance seat = seatInstanceRepository.findByIdWithCabin(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.SEAT_INSTANCE_NOT_FOUND, "SeatInstance", id
                ));

        SeatStatus current = seat.getStatus();
        SeatStatus target = request.getStatus();

        if (!isValidTransition(current, target)) {
            throw new ValidationException(ErrorCode.INVALID_SEAT_STATUS_TRANSITION,
                    "Cannot transition from " + current + " to " + target);
        }

        seat.setStatus(target);
        SeatInstance updated = seatInstanceRepository.save(seat);

        recalculateAvailableSeats(seat.getCabin().getId());

        return seatInstanceMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public SeatInstanceResponse assign(Long id, SeatAssignmentRequest request) {
        SeatInstance seat = seatInstanceRepository.findByIdWithCabin(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.SEAT_INSTANCE_NOT_FOUND, "SeatInstance", id
                ));

        if (seat.getStatus() != SeatStatus.AVAILABLE && seat.getStatus() != SeatStatus.RESERVED) {
            throw new ValidationException(ErrorCode.SEAT_NOT_AVAILABLE,
                    "Seat " + seat.getSeatNumber() + " is not available (current status: " + seat.getStatus() + ")");
        }

        seat.setBookingId(request.getBookingId());
        seat.setPassengerId(request.getPassengerId());
        seat.setStatus(SeatStatus.OCCUPIED);

        SeatInstance updated = seatInstanceRepository.save(seat);
        recalculateAvailableSeats(seat.getCabin().getId());

        return seatInstanceMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public SeatInstanceResponse release(Long id) {
        SeatInstance seat = seatInstanceRepository.findByIdWithCabin(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.SEAT_INSTANCE_NOT_FOUND, "SeatInstance", id
                ));

        seat.setBookingId(null);
        seat.setPassengerId(null);
        seat.setStatus(SeatStatus.AVAILABLE);

        SeatInstance updated = seatInstanceRepository.save(seat);
        recalculateAvailableSeats(seat.getCabin().getId());

        return seatInstanceMapper.toResponse(updated);
    }

    private void assertCabinExists(Long cabinId) {
        if (!cabinRepository.existsById(cabinId)) {
            throw new ResourceNotFoundException(ErrorCode.CABIN_NOT_FOUND, "Cabin", cabinId);
        }
    }

    /**
     * Transiciones permitidas:
     * AVAILABLE <--> BLOCKED
     * AVAILABLE <--> RESERVED
     * AVAILABLE  --> OCCUPIED
     * RESERVED   --> OCCUPIED
     * RESERVED   --> AVAILABLE
     * OCCUPIED   --> AVAILABLE  (release)
     * BLOCKED    --> AVAILABLE
     * RESERVED   --> BLOCKED
     * BLOCKED    --> RESERVED
     */
    private boolean isValidTransition(SeatStatus from, SeatStatus to) {
        if (from == to) return true;
        return switch (from) {
            case AVAILABLE -> to == SeatStatus.BLOCKED
                    || to == SeatStatus.RESERVED
                    || to == SeatStatus.OCCUPIED;
            case RESERVED -> to == SeatStatus.AVAILABLE
                    || to == SeatStatus.OCCUPIED
                    || to == SeatStatus.BLOCKED;
            case OCCUPIED -> to == SeatStatus.AVAILABLE;
            case BLOCKED -> to == SeatStatus.AVAILABLE
                    || to == SeatStatus.RESERVED;
        };
    }

    private void recalculateAvailableSeats(Long cabinId) {
        Cabin cabin = cabinRepository.findById(cabinId).orElseThrow();
        long available = seatInstanceRepository.countByCabinIdAndStatus(cabinId, SeatStatus.AVAILABLE);
        cabin.setAvailableSeats((int) available);
        cabinRepository.save(cabin);
    }

}