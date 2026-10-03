package com.airline.service.impl;

import com.airline.config.SeatProperties;
import com.airline.enums.CabinClass;
import com.airline.enums.SeatStatus;
import com.airline.enums.SeatType;
import com.airline.exception.ResourceAlreadyExistsException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.ValidationException;
import com.airline.mapper.CabinMapper;
import com.airline.model.Cabin;
import com.airline.model.SeatInstance;
import com.airline.model.SeatMap;
import com.airline.payload.request.CabinRequest;
import com.airline.payload.response.CabinAvailabilityResponse;
import com.airline.payload.response.CabinResponse;
import com.airline.repository.CabinRepository;
import com.airline.repository.SeatInstanceRepository;
import com.airline.repository.SeatMapRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CabinServiceImpl Tests")
class CabinServiceImplTest {

    @Mock
    private CabinRepository cabinRepository;
    @Mock
    private SeatMapRepository seatMapRepository;
    @Mock
    private SeatInstanceRepository seatInstanceRepository;
    @Mock
    private CabinMapper cabinMapper;
    @Mock
    private SeatProperties seatProperties;

    @InjectMocks
    private CabinServiceImpl cabinService;

    private CabinRequest validRequest;
    private Cabin cabinEntity;
    private CabinResponse cabinResponse;

    @BeforeEach
    void setUp() {
        validRequest = CabinRequest.builder()
                .flightInstanceId(1L)
                .cabinClass(CabinClass.ECONOMY)
                .rowStart(1)
                .rowEnd(10)
                .columnLayout("ABC-DEF")
                .basePrice(new BigDecimal("50.00"))
                .build();

        cabinEntity = Cabin.builder()
                .id(1L)
                .flightInstanceId(1L)
                .cabinClass(CabinClass.ECONOMY)
                .rowStart(1)
                .rowEnd(10)
                .columnLayout("ABC-DEF")
                .totalSeats(60)
                .availableSeats(60)
                .basePrice(new BigDecimal("50.00"))
                .currency("EUR")
                .active(true)
                .build();

        cabinResponse = CabinResponse.builder()
                .id(1L)
                .flightInstanceId(1L)
                .cabinClass(CabinClass.ECONOMY)
                .rowStart(1)
                .rowEnd(10)
                .columnLayout("ABC-DEF")
                .totalSeats(60)
                .availableSeats(60)
                .build();

        when(seatProperties.getDefaultCurrency()).thenReturn("EUR");
        when(seatProperties.getDefaultActive()).thenReturn(true);
        when(seatProperties.getDefaultPriceModifier()).thenReturn(BigDecimal.ZERO);
        when(seatProperties.getExitRowStart()).thenReturn(0);
        when(seatProperties.getExitRowEnd()).thenReturn(0);
    }

    @Nested
    @DisplayName("createCabin")
    class CreateCabinTests {

        @Test
        @DisplayName("Should create cabin and generate 60 seats for ABC-DEF x 10 rows")
        void shouldCreateCabinAndGenerateSeats() {
            when(cabinRepository.existsByFlightInstanceIdAndCabinClass(1L, CabinClass.ECONOMY))
                    .thenReturn(false);
            when(cabinMapper.toEntity(validRequest)).thenReturn(cabinEntity);
            when(cabinRepository.save(cabinEntity)).thenReturn(cabinEntity);
            when(seatMapRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            when(seatInstanceRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            when(cabinMapper.toResponse(cabinEntity)).thenReturn(cabinResponse);

            CabinResponse result = cabinService.createCabin(validRequest, 1L);

            assertThat(result).isNotNull();
            assertThat(cabinEntity.getTotalSeats()).isEqualTo(60);
            assertThat(cabinEntity.getAvailableSeats()).isEqualTo(60);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<SeatMap>> mapsCaptor = ArgumentCaptor.forClass(List.class);
            verify(seatMapRepository).saveAll(mapsCaptor.capture());
            assertThat(mapsCaptor.getValue()).hasSize(10);

            // 10 filas x 6 letras = 60 SeatInstances
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<SeatInstance>> seatsCaptor = ArgumentCaptor.forClass(List.class);
            verify(seatInstanceRepository).saveAll(seatsCaptor.capture());
            assertThat(seatsCaptor.getValue()).hasSize(60);
        }

        @Test
        @DisplayName("Should assign correct seat types: A=WINDOW B=MIDDLE C=AISLE D=AISLE E=MIDDLE F=WINDOW")
        void shouldAssignCorrectSeatTypes() {
            when(cabinRepository.existsByFlightInstanceIdAndCabinClass(1L, CabinClass.ECONOMY))
                    .thenReturn(false);
            when(cabinMapper.toEntity(validRequest)).thenReturn(cabinEntity);
            when(cabinRepository.save(cabinEntity)).thenReturn(cabinEntity);
            when(seatMapRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            when(seatInstanceRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            when(cabinMapper.toResponse(cabinEntity)).thenReturn(cabinResponse);

            cabinService.createCabin(validRequest, 1L);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<SeatInstance>> seatsCaptor = ArgumentCaptor.forClass(List.class);
            verify(seatInstanceRepository).saveAll(seatsCaptor.capture());

            List<SeatInstance> seats = seatsCaptor.getValue();

            // Fila 1
            SeatInstance a = findByNumber(seats, "1A");
            SeatInstance b = findByNumber(seats, "1B");
            SeatInstance c = findByNumber(seats, "1C");
            SeatInstance d = findByNumber(seats, "1D");
            SeatInstance e = findByNumber(seats, "1E");
            SeatInstance f = findByNumber(seats, "1F");

            assertThat(a.getSeatType()).isEqualTo(SeatType.WINDOW);
            assertThat(b.getSeatType()).isEqualTo(SeatType.MIDDLE);
            assertThat(c.getSeatType()).isEqualTo(SeatType.AISLE);
            assertThat(d.getSeatType()).isEqualTo(SeatType.AISLE);
            assertThat(e.getSeatType()).isEqualTo(SeatType.MIDDLE);
            assertThat(f.getSeatType()).isEqualTo(SeatType.WINDOW);

            // Todos AVAILABLE y sin booking
            assertThat(seats).allMatch(s -> s.getStatus() == SeatStatus.AVAILABLE);
            assertThat(seats).allMatch(s -> s.getBookingId() == null);
        }

        @Test
        @DisplayName("Should throw when cabin already exists for flightInstance+cabinClass")
        void shouldThrowWhenCabinExists() {
            when(cabinRepository.existsByFlightInstanceIdAndCabinClass(1L, CabinClass.ECONOMY))
                    .thenReturn(true);

            assertThatThrownBy(() -> cabinService.createCabin(validRequest, 1L))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("already exists");

            verify(cabinRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when rowStart > rowEnd")
        void shouldThrowWhenInvalidRows() {
            CabinRequest invalid = CabinRequest.builder()
                    .flightInstanceId(1L)
                    .cabinClass(CabinClass.ECONOMY)
                    .rowStart(10)
                    .rowEnd(1)
                    .columnLayout("ABC")
                    .basePrice(BigDecimal.TEN)
                    .build();

            assertThatThrownBy(() -> cabinService.createCabin(invalid, 1L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Row start cannot be greater than row end");
        }

        private SeatInstance findByNumber(List<SeatInstance> seats, String number) {
            return seats.stream()
                    .filter(s -> s.getSeatNumber().equals(number))
                    .findFirst()
                    .orElseThrow();
        }
    }

    @Nested
    @DisplayName("getCabinById")
    class GetCabinByIdTests {

        @Test
        @DisplayName("Should return cabin when exists")
        void shouldReturnCabin() {
            when(cabinRepository.findById(1L)).thenReturn(Optional.of(cabinEntity));
            when(cabinMapper.toResponse(cabinEntity)).thenReturn(cabinResponse);

            CabinResponse result = cabinService.getCabinById(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw when cabin not found")
        void shouldThrowWhenNotFound() {
            when(cabinRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cabinService.getCabinById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cabin not found with id: 999");
        }
    }

    @Nested
    @DisplayName("getAvailability")
    class GetAvailabilityTests {

        @Test
        @DisplayName("Should compute availability from grouped counts")
        void shouldComputeAvailability() {
            when(cabinRepository.findById(1L)).thenReturn(Optional.of(cabinEntity));
            when(seatInstanceRepository.countByCabinIdGroupedByStatus(1L))
                    .thenReturn(List.of(
                            new Object[]{SeatStatus.AVAILABLE, 40L},
                            new Object[]{SeatStatus.OCCUPIED, 15L},
                            new Object[]{SeatStatus.BLOCKED, 3L},
                            new Object[]{SeatStatus.RESERVED, 2L}
                    ));

            CabinAvailabilityResponse result = cabinService.getAvailability(1L);

            assertThat(result.getAvailableSeats()).isEqualTo(40);
            assertThat(result.getOccupiedSeats()).isEqualTo(15);
            assertThat(result.getBlockedSeats()).isEqualTo(3);
            assertThat(result.getReservedSeats()).isEqualTo(2);
            assertThat(result.getTotalSeats()).isEqualTo(60);
        }
    }

    @Nested
    @DisplayName("deleteCabin")
    class DeleteCabinTests {

        @Test
        @DisplayName("Should delete when no assigned seats")
        void shouldDelete() {
            when(cabinRepository.findById(1L)).thenReturn(Optional.of(cabinEntity));
            when(seatInstanceRepository.existsByCabinIdAndBookingIdIsNotNull(1L)).thenReturn(false);

            cabinService.deleteCabin(1L, 1L);

            verify(cabinRepository).delete(cabinEntity);
        }

        @Test
        @DisplayName("Should throw when cabin has assigned seats")
        void shouldThrowWhenHasAssignedSeats() {
            when(cabinRepository.findById(1L)).thenReturn(Optional.of(cabinEntity));
            when(seatInstanceRepository.existsByCabinIdAndBookingIdIsNotNull(1L)).thenReturn(true);

            assertThatThrownBy(() -> cabinService.deleteCabin(1L, 1L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("assigned seats");

            verify(cabinRepository, never()).delete(any());
        }
    }
}