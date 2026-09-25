package com.airline.service.impl;

import com.airline.enums.AircraftStatus;
import com.airline.enums.AirlineStatus;
import com.airline.exception.ResourceAlreadyExistsException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.ValidationException;
import com.airline.mapper.AircraftMapper;
import com.airline.model.Aircraft;
import com.airline.model.Airline;
import com.airline.payload.request.AircraftRequest;
import com.airline.payload.response.AircraftResponse;
import com.airline.repository.AircraftRepository;
import com.airline.repository.AirlineRepository;
import com.airline.service.AircraftMaintenanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AircraftServiceImpl Tests")
class AircraftServiceImplTest {

    @Mock
    private AircraftRepository aircraftRepository;
    @Mock
    private AirlineRepository airlineRepository;
    @Mock
    private AircraftMapper aircraftMapper;
    @Mock
    private AircraftMaintenanceService maintenanceService;

    @InjectMocks
    private AircraftServiceImpl aircraftService;

    private AircraftRequest validRequest;
    private Aircraft aircraft;
    private AircraftResponse aircraftResponse;
    private Airline airline;

    @BeforeEach
    void setUp() {
        validRequest = AircraftRequest.builder()
                .code("EC-AAA")
                .model("Boeing 737-800")
                .manufacturer("Boeing")
                .economySeats(150)
                .businessSeats(16)
                .airlineId(1L)
                .build();

        airline = Airline.builder()
                .id(1L)
                .iataCode("AA")
                .icaoCode("AAL")
                .name("American Airlines")
                .status(AirlineStatus.ACTIVE)
                .ownerId(1L)
                .build();

        aircraft = Aircraft.builder()
                .id(1L)
                .code("EC-AAA")
                .model("Boeing 737-800")
                .manufacturer("Boeing")
                .economySeats(150)
                .businessSeats(16)
                .status(AircraftStatus.ACTIVE)
                .available(true)
                .airline(airline)
                .build();

        aircraftResponse = AircraftResponse.builder()
                .id(1L)
                .code("EC-AAA")
                .model("Boeing 737-800")
                .manufacturer("Boeing")
                .totalSeats(166)
                .status(AircraftStatus.ACTIVE)
                .available(true)
                .operational(true)
                .airlineId(1L)
                .build();
    }

    @Nested
    @DisplayName("createAircraft")
    class CreateAircraftTests {

        @Test
        @DisplayName("Should create aircraft successfully")
        void shouldCreateAircraftSuccessfully() {
            when(aircraftRepository.existsByCode("EC-AAA")).thenReturn(false);
            when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));
            when(aircraftMapper.toEntity(validRequest)).thenReturn(aircraft);
            when(aircraftRepository.save(aircraft)).thenReturn(aircraft);
            when(aircraftMapper.toResponse(aircraft)).thenReturn(aircraftResponse);
            when(maintenanceService.requiresMaintenance(aircraft)).thenReturn(false);

            AircraftResponse result = aircraftService.createAircraft(validRequest, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("EC-AAA");
            verify(aircraftRepository).save(aircraft);
        }

        @Test
        @DisplayName("Should throw when code exists")
        void shouldThrowWhenCodeExists() {
            when(aircraftRepository.existsByCode("EC-AAA")).thenReturn(true);

            assertThatThrownBy(() -> aircraftService.createAircraft(validRequest, 1L))
                    .isInstanceOf(ResourceAlreadyExistsException.class);

            verify(aircraftRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when airline not found")
        void shouldThrowWhenAirlineNotFound() {
            when(aircraftRepository.existsByCode("EC-AAA")).thenReturn(false);
            when(airlineRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> aircraftService.createAircraft(validRequest, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw when requester is not owner")
        void shouldThrowWhenNotOwner() {
            when(aircraftRepository.existsByCode("EC-AAA")).thenReturn(false);
            when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));

            assertThatThrownBy(() -> aircraftService.createAircraft(validRequest, 99L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("You don't have permission");
        }
    }

    @Nested
    @DisplayName("changeStatus")
    class ChangeStatusTests {

        @Test
        @DisplayName("Should change status ACTIVE → MAINTENANCE")
        void shouldChangeStatusToMaintenance() {
            when(aircraftRepository.findById(1L)).thenReturn(Optional.of(aircraft));
            when(aircraftRepository.save(aircraft)).thenReturn(aircraft);
            when(aircraftMapper.toResponse(aircraft)).thenReturn(aircraftResponse);
            when(maintenanceService.requiresMaintenance(aircraft)).thenReturn(false);

            aircraftService.changeStatus(1L, AircraftStatus.MAINTENANCE, 1L);

            assertThat(aircraft.getStatus()).isEqualTo(AircraftStatus.MAINTENANCE);
            verify(aircraftRepository).save(aircraft);
        }

        @Test
        @DisplayName("Should throw when RETIRED → ACTIVE")
        void shouldThrowWhenRetiredToActive() {
            aircraft.setStatus(AircraftStatus.RETIRED);
            when(aircraftRepository.findById(1L)).thenReturn(Optional.of(aircraft));

            assertThatThrownBy(() ->
                    aircraftService.changeStatus(1L, AircraftStatus.ACTIVE, 1L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Cannot transition from RETIRED to ACTIVE");
        }
    }

    @Nested
    @DisplayName("deleteAircraft")
    class DeleteAircraftTests {

        @Test
        @DisplayName("Should delete aircraft successfully")
        void shouldDeleteAircraftSuccessfully() {
            when(aircraftRepository.findById(1L)).thenReturn(Optional.of(aircraft));

            aircraftService.deleteAircraft(1L, 1L);

            verify(aircraftRepository).delete(aircraft);
        }

        @Test
        @DisplayName("Should throw when not owner")
        void shouldThrowWhenNotOwner() {
            when(aircraftRepository.findById(1L)).thenReturn(Optional.of(aircraft));

            assertThatThrownBy(() -> aircraftService.deleteAircraft(1L, 99L))
                    .isInstanceOf(ValidationException.class);
        }
    }

}