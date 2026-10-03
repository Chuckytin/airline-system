package com.airline.service.impl;

import com.airline.enums.SeatStatus;
import com.airline.enums.SeatType;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SeatInstanceServiceImpl Tests")
class SeatInstanceServiceImplTest {

    @Mock
    private SeatInstanceRepository seatInstanceRepository;
    @Mock
    private CabinRepository cabinRepository;
    @Mock
    private SeatInstanceMapper seatInstanceMapper;

    @InjectMocks
    private SeatInstanceServiceImpl seatInstanceService;

    private Cabin cabin;
    private SeatInstance seat;
    private SeatInstanceResponse seatResponse;

    @BeforeEach
    void setUp() {
        cabin = Cabin.builder()
                .id(1L)
                .availableSeats(60)
                .build();

        seat = SeatInstance.builder()
                .id(10L)
                .cabin(cabin)
                .seatNumber("12A")
                .seatRow(12)
                .columnLetter("A")
                .seatType(SeatType.WINDOW)
                .status(SeatStatus.AVAILABLE)
                .build();

        seatResponse = SeatInstanceResponse.builder()
                .id(10L)
                .cabinId(1L)
                .seatNumber("12A")
                .status(SeatStatus.AVAILABLE)
                .build();
    }

    @Nested
    @DisplayName("changeStatus")
    class ChangeStatusTests {

        @Test
        @DisplayName("AVAILABLE -> BLOCKED should succeed")
        void availableToBlocked() {
            when(seatInstanceRepository.findByIdWithCabin(10L)).thenReturn(Optional.of(seat));
            when(seatInstanceRepository.save(seat)).thenReturn(seat);
            when(seatInstanceRepository.countByCabinIdAndStatus(1L, SeatStatus.AVAILABLE)).thenReturn(59L);
            when(cabinRepository.findById(1L)).thenReturn(Optional.of(cabin));
            when(seatInstanceMapper.toResponse(seat)).thenReturn(seatResponse);

            seatInstanceService.changeStatus(10L,
                    new SeatStatusUpdateRequest(SeatStatus.BLOCKED));

            assertThat(seat.getStatus()).isEqualTo(SeatStatus.BLOCKED);
        }

        @Test
        @DisplayName("OCCUPIED -> RESERVED should fail")
        void occupiedToReservedShouldFail() {
            seat.setStatus(SeatStatus.OCCUPIED);
            when(seatInstanceRepository.findByIdWithCabin(10L)).thenReturn(Optional.of(seat));

            assertThatThrownBy(() -> seatInstanceService.changeStatus(10L,
                    new SeatStatusUpdateRequest(SeatStatus.RESERVED)))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Cannot transition");

            verify(seatInstanceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when seat not found")
        void notFound() {
            when(seatInstanceRepository.findByIdWithCabin(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> seatInstanceService.changeStatus(999L,
                    new SeatStatusUpdateRequest(SeatStatus.BLOCKED)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("assign")
    class AssignTests {

        @Test
        @DisplayName("AVAILABLE -> assign should set OCCUPIED and booking/passenger")
        void assignFromAvailable() {
            when(seatInstanceRepository.findByIdWithCabin(10L)).thenReturn(Optional.of(seat));
            when(seatInstanceRepository.save(seat)).thenReturn(seat);
            when(seatInstanceRepository.countByCabinIdAndStatus(1L, SeatStatus.AVAILABLE)).thenReturn(59L);
            when(cabinRepository.findById(1L)).thenReturn(Optional.of(cabin));
            when(seatInstanceMapper.toResponse(seat)).thenReturn(seatResponse);

            seatInstanceService.assign(10L, new SeatAssignmentRequest(100L, 200L));

            assertThat(seat.getStatus()).isEqualTo(SeatStatus.OCCUPIED);
            assertThat(seat.getBookingId()).isEqualTo(100L);
            assertThat(seat.getPassengerId()).isEqualTo(200L);
        }

        @Test
        @DisplayName("BLOCKED -> assign should fail")
        void assignFromBlockedShouldFail() {
            seat.setStatus(SeatStatus.BLOCKED);
            when(seatInstanceRepository.findByIdWithCabin(10L)).thenReturn(Optional.of(seat));

            assertThatThrownBy(() -> seatInstanceService.assign(10L,
                    new SeatAssignmentRequest(100L, 200L)))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("not available");
        }
    }

    @Nested
    @DisplayName("release")
    class ReleaseTests {

        @Test
        @DisplayName("OCCUPIED -> release should reset to AVAILABLE and clear booking/passenger")
        void releaseOccupied() {
            seat.setStatus(SeatStatus.OCCUPIED);
            seat.setBookingId(100L);
            seat.setPassengerId(200L);

            when(seatInstanceRepository.findByIdWithCabin(10L)).thenReturn(Optional.of(seat));
            when(seatInstanceRepository.save(seat)).thenReturn(seat);
            when(seatInstanceRepository.countByCabinIdAndStatus(1L, SeatStatus.AVAILABLE)).thenReturn(60L);
            when(cabinRepository.findById(1L)).thenReturn(Optional.of(cabin));
            when(seatInstanceMapper.toResponse(seat)).thenReturn(seatResponse);

            seatInstanceService.release(10L);

            assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(seat.getBookingId()).isNull();
            assertThat(seat.getPassengerId()).isNull();
        }
    }
}