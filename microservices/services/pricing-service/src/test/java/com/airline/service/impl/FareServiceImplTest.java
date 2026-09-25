package com.airline.service.impl;

import com.airline.config.PricingProperties;
import com.airline.enums.CabinClass;
import com.airline.enums.FareType;
import com.airline.exception.ResourceAlreadyExistsException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.exception.ValidationException;
import com.airline.mapper.FareMapper;
import com.airline.model.Fare;
import com.airline.payload.request.FareRequest;
import com.airline.payload.response.FareResponse;
import com.airline.repository.FareRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FareServiceImpl Tests")
class FareServiceImplTest {

    @Mock
    private FareRepository fareRepository;

    @Mock
    private FareMapper fareMapper;

    @Mock
    private PricingProperties pricingProperties;

    @InjectMocks
    private FareServiceImpl fareService;

    private FareRequest validRequest;
    private Fare fareEntity;
    private FareResponse fareResponse;

    @BeforeEach
    void setUp() {
        validRequest = FareRequest.builder()
                .flightId(1L)
                .cabinClass(CabinClass.ECONOMY)
                .fareType(FareType.STANDARD)
                .basePrice(new BigDecimal("100.00"))
                .taxes(new BigDecimal("21.00"))
                .currency("EUR")
                .validFrom(LocalDate.now())
                .validUntil(LocalDate.now().plusMonths(6))
                .build();

        fareEntity = Fare.builder()
                .id(1L)
                .flightId(1L)
                .cabinClass(CabinClass.ECONOMY)
                .fareType(FareType.STANDARD)
                .basePrice(new BigDecimal("100.00"))
                .taxes(new BigDecimal("21.00"))
                .currency("EUR")
                .validFrom(LocalDate.now())
                .active(true)
                .build();

        fareResponse = FareResponse.builder()
                .id(1L)
                .flightId(1L)
                .cabinClass(CabinClass.ECONOMY)
                .fareType(FareType.STANDARD)
                .basePrice(new BigDecimal("100.00"))
                .taxes(new BigDecimal("21.00"))
                .totalPrice(new BigDecimal("121.00"))
                .currency("EUR")
                .active(true)
                .build();

        when(pricingProperties.getDefaultCurrency()).thenReturn("EUR");
    }

    @Nested
    @DisplayName("createFare")
    class CreateFareTests {

        @Test
        @DisplayName("Should create fare successfully")
        void shouldCreateFareSuccessfully() {
            when(fareRepository.existsByFlightIdAndCabinClassAndFareType(
                    1L, CabinClass.ECONOMY, FareType.STANDARD)).thenReturn(false);
            when(fareMapper.toEntity(validRequest)).thenReturn(fareEntity);
            when(fareRepository.save(fareEntity)).thenReturn(fareEntity);
            when(fareMapper.toResponse(fareEntity)).thenReturn(fareResponse);

            FareResponse result = fareService.createFare(validRequest, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(fareRepository).save(fareEntity);
        }

        @Test
        @DisplayName("Should throw when fare already exists")
        void shouldThrowWhenFareExists() {
            when(fareRepository.existsByFlightIdAndCabinClassAndFareType(
                    1L, CabinClass.ECONOMY, FareType.STANDARD)).thenReturn(true);

            assertThatThrownBy(() -> fareService.createFare(validRequest, 1L))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("Fare already exists");

            verify(fareRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when validUntil is before validFrom")
        void shouldThrowWhenInvalidDateRange() {
            FareRequest invalidRequest = FareRequest.builder()
                    .flightId(1L)
                    .cabinClass(CabinClass.ECONOMY)
                    .fareType(FareType.STANDARD)
                    .basePrice(new BigDecimal("100.00"))
                    .taxes(new BigDecimal("21.00"))
                    .validFrom(LocalDate.now().plusMonths(6))
                    .validUntil(LocalDate.now())   // <--- Antes del validFrom
                    .build();

            when(fareRepository.existsByFlightIdAndCabinClassAndFareType(
                    1L, CabinClass.ECONOMY, FareType.STANDARD)).thenReturn(false);

            assertThatThrownBy(() -> fareService.createFare(invalidRequest, 1L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Valid until must be after valid from");
        }

    }

    @Nested
    @DisplayName("getFareById")
    class GetFareByIdTests {

        @Test
        @DisplayName("Should return fare when exists")
        void shouldReturnFareWhenExists() {
            when(fareRepository.findById(1L)).thenReturn(Optional.of(fareEntity));
            when(fareMapper.toResponse(fareEntity)).thenReturn(fareResponse);

            FareResponse result = fareService.getFareById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw when fare not found")
        void shouldThrowWhenNotFound() {
            when(fareRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> fareService.getFareById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Fare not found with id: 999");
        }

    }

}