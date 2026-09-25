package com.airline.service.impl;

import com.airline.enums.AirlineStatus;
import com.airline.exception.ResourceAlreadyExistsException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.ValidationException;
import com.airline.mapper.AirlineMapper;
import com.airline.model.Airline;
import com.airline.payload.request.AirlineRequest;
import com.airline.payload.response.AirlineResponse;
import com.airline.repository.AirlineRepository;
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
@DisplayName("AirlineServiceImpl Tests")
class AirlineServiceImplTest {

    @Mock
    private AirlineRepository airlineRepository;

    @Mock
    private AirlineMapper airlineMapper;

    @InjectMocks
    private AirlineServiceImpl airlineService;

    private AirlineRequest validRequest;
    private Airline airline;
    private AirlineResponse airlineResponse;

    @BeforeEach
    void setUp() {
        validRequest = AirlineRequest.builder()
                .iataCode("AA")
                .icaoCode("AAL")
                .name("American Airlines")
                .alias("American")
                .logoUrl("https://example.com/aa.png")
                .website("https://www.aa.com")
                .status(AirlineStatus.ACTIVE)
                .alliance("Oneworld")
                .headquartersCityId(1L)
                .build();

        airline = Airline.builder()
                .id(1L)
                .iataCode("AA")
                .icaoCode("AAL")
                .name("American Airlines")
                .status(AirlineStatus.ACTIVE)
                .ownerId(1L)
                .build();

        airlineResponse = AirlineResponse.builder()
                .id(1L)
                .iataCode("AA")
                .icaoCode("AAL")
                .name("American Airlines")
                .status(AirlineStatus.ACTIVE)
                .ownerId(1L)
                .build();
    }

    @Nested
    @DisplayName("createAirline")
    class CreateAirlineTests {

        @Test
        @DisplayName("Should create airline successfully")
        void shouldCreateAirlineSuccessfully() {
            when(airlineRepository.existsByIataCode("AA")).thenReturn(false);
            when(airlineRepository.existsByIcaoCode("AAL")).thenReturn(false);
            when(airlineRepository.existsByOwnerId(1L)).thenReturn(false);
            when(airlineMapper.toEntity(validRequest)).thenReturn(airline);
            when(airlineRepository.save(airline)).thenReturn(airline);
            when(airlineMapper.toResponse(airline)).thenReturn(airlineResponse);

            AirlineResponse result = airlineService.createAirline(validRequest, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getIataCode()).isEqualTo("AA");
            verify(airlineRepository).save(airline);
        }

        @Test
        @DisplayName("Should throw when IATA code exists")
        void shouldThrowWhenIataCodeExists() {
            when(airlineRepository.existsByIataCode("AA")).thenReturn(true);

            assertThatThrownBy(() -> airlineService.createAirline(validRequest, 1L))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("Airline already exists with iataCode: AA");

            verify(airlineRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when ICAO code exists")
        void shouldThrowWhenIcaoCodeExists() {
            when(airlineRepository.existsByIataCode("AA")).thenReturn(false);
            when(airlineRepository.existsByIcaoCode("AAL")).thenReturn(true);

            assertThatThrownBy(() -> airlineService.createAirline(validRequest, 1L))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("Airline already exists with icaoCode: AAL");

            verify(airlineRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when owner already has airline")
        void shouldThrowWhenOwnerAlreadyHasAirline() {
            when(airlineRepository.existsByIataCode("AA")).thenReturn(false);
            when(airlineRepository.existsByIcaoCode("AAL")).thenReturn(false);
            when(airlineRepository.existsByOwnerId(1L)).thenReturn(true);

            assertThatThrownBy(() -> airlineService.createAirline(validRequest, 1L))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("Airline already exists with ownerId: 1");

            verify(airlineRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAirlineById")
    class GetAirlineByIdTests {

        @Test
        @DisplayName("Should return airline when exists")
        void shouldReturnAirlineWhenExists() {
            when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));
            when(airlineMapper.toResponse(airline)).thenReturn(airlineResponse);

            AirlineResponse result = airlineService.getAirlineById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw when not found")
        void shouldThrowWhenNotFound() {
            when(airlineRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> airlineService.getAirlineById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Airline not found with id: 999");
        }
    }

    @Nested
    @DisplayName("getAirlineByOwnerId")
    class GetAirlineByOwnerIdTests {

        @Test
        @DisplayName("Should return airline when owner has one")
        void shouldReturnAirlineWhenOwnerHasOne() {
            when(airlineRepository.findByOwnerId(1L)).thenReturn(Optional.of(airline));
            when(airlineMapper.toResponse(airline)).thenReturn(airlineResponse);

            AirlineResponse result = airlineService.getAirlineByOwnerId(1L);

            assertThat(result).isNotNull();
            assertThat(result.getOwnerId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw when owner has no airline")
        void shouldThrowWhenOwnerHasNoAirline() {
            when(airlineRepository.findByOwnerId(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> airlineService.getAirlineByOwnerId(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateAirline")
    class UpdateAirlineTests {

        @Test
        @DisplayName("Should update airline successfully")
        void shouldUpdateAirlineSuccessfully() {
            when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));
            when(airlineRepository.save(airline)).thenReturn(airline);
            when(airlineMapper.toResponse(airline)).thenReturn(airlineResponse);

            AirlineResponse result = airlineService.updateAirline(1L, validRequest, 1L);

            assertThat(result).isNotNull();
            verify(airlineMapper).updateEntity(airline, validRequest);
            verify(airlineRepository).save(airline);
        }

        @Test
        @DisplayName("Should throw when airline not found")
        void shouldThrowWhenNotFound() {
            when(airlineRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> airlineService.updateAirline(999L, validRequest, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw when requester is not the owner")
        void shouldThrowWhenNotOwner() {
            when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));

            assertThatThrownBy(() -> airlineService.updateAirline(1L, validRequest, 99L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("You don't have permission");
        }
    }

    @Nested
    @DisplayName("deleteAirline")
    class DeleteAirlineTests {

        @Test
        @DisplayName("Should delete airline successfully")
        void shouldDeleteAirlineSuccessfully() {
            when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));

            airlineService.deleteAirline(1L, 1L);

            verify(airlineRepository).delete(airline);
        }

        @Test
        @DisplayName("Should throw when not owner")
        void shouldThrowWhenNotOwner() {
            when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));

            assertThatThrownBy(() -> airlineService.deleteAirline(1L, 99L))
                    .isInstanceOf(ValidationException.class);

            verify(airlineRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("changeStatusByAdmin")
    class ChangeStatusTests {

        @Test
        @DisplayName("Should change status ACTIVE → INACTIVE")
        void shouldChangeStatusActiveToInactive() {
            when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));
            when(airlineRepository.save(airline)).thenReturn(airline);
            when(airlineMapper.toResponse(airline)).thenReturn(airlineResponse);

            airlineService.changeStatusByAdmin(1L, AirlineStatus.INACTIVE, 1L);

            verify(airlineRepository).save(airline);
            assertThat(airline.getStatus()).isEqualTo(AirlineStatus.INACTIVE);
        }

        @Test
        @DisplayName("Should throw when same status")
        void shouldThrowWhenSameStatus() {
            when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));

            assertThatThrownBy(() ->
                    airlineService.changeStatusByAdmin(1L, AirlineStatus.ACTIVE, 1L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("already in status");
        }

        @Test
        @DisplayName("Should throw when invalid transition")
        void shouldThrowWhenInvalidTransition() {
            airline.setStatus(AirlineStatus.BANNED);
            when(airlineRepository.findById(1L)).thenReturn(Optional.of(airline));

            assertThatThrownBy(() ->
                    airlineService.changeStatusByAdmin(1L, AirlineStatus.ACTIVE, 1L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Cannot transition from BANNED to ACTIVE");
        }
    }

}