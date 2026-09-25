package com.airline.repository;

import com.airline.config.JpaAuditingConfig;
import com.airline.enums.CabinClass;
import com.airline.enums.FareType;
import com.airline.model.Fare;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("FareRepository Tests")
class FareRepositoryTest {

    @Autowired
    private FareRepository fareRepository;

    private Fare fare;

    @BeforeEach
    void setUp() {
        fare = Fare.builder()
                .flightId(1L)
                .cabinClass(CabinClass.ECONOMY)
                .fareType(FareType.STANDARD)
                .basePrice(new BigDecimal("100.00"))
                .taxes(new BigDecimal("21.00"))
                .currency("EUR")
                .validFrom(LocalDate.now())
                .validUntil(LocalDate.now().plusMonths(6))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should save and find fare by ID")
    void shouldSaveAndFindById() {
        Fare saved = fareRepository.save(fare);

        assertThat(saved.getId()).isNotNull();
        Optional<Fare> found = fareRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCabinClass()).isEqualTo(CabinClass.ECONOMY);
    }

    @Test
    @DisplayName("Should find fare by flightId+cabinClass+fareType")
    void shouldFindByFlightCabinFareType() {
        fareRepository.save(fare);

        Optional<Fare> found = fareRepository.findByFlightIdAndCabinClassAndFareType(
                1L, CabinClass.ECONOMY, FareType.STANDARD);

        assertThat(found).isPresent();
        assertThat(found.get().getBasePrice()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Should return empty when fare not found")
    void shouldReturnEmptyWhenNotFound() {
        Optional<Fare> found = fareRepository.findByFlightIdAndCabinClassAndFareType(
                999L, CabinClass.ECONOMY, FareType.STANDARD);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check exists by flightId+cabinClass+fareType")
    void shouldCheckExists() {
        fareRepository.save(fare);

        boolean exists = fareRepository.existsByFlightIdAndCabinClassAndFareType(
                1L, CabinClass.ECONOMY, FareType.STANDARD);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should find active fares for date")
    void shouldFindActiveFaresForDate() {
        fareRepository.save(fare);

        List<Fare> active = fareRepository.findActiveFaresForDate(1L, LocalDate.now());

        assertThat(active).hasSize(1);
        assertThat(active.getFirst().getCabinClass()).isEqualTo(CabinClass.ECONOMY);
    }

    @Test
    @DisplayName("Should return empty when date is out of valid range")
    void shouldReturnEmptyWhenDateOutOfRange() {
        fareRepository.save(fare);

        List<Fare> active = fareRepository.findActiveFaresForDate(
                1L, LocalDate.now().plusYears(2));

        assertThat(active).isEmpty();
    }

    @Test
    @DisplayName("Should find fares by flightId")
    void shouldFindByFlightId() {
        fareRepository.save(fare);

        Fare fare2 = Fare.builder()
                .flightId(1L)
                .cabinClass(CabinClass.BUSINESS)
                .fareType(FareType.FLEX)
                .basePrice(new BigDecimal("500.00"))
                .taxes(new BigDecimal("100.00"))
                .currency("EUR")
                .validFrom(LocalDate.now())
                .active(true)
                .build();
        fareRepository.save(fare2);

        List<Fare> fares = fareRepository.findByFlightId(1L);

        assertThat(fares).hasSize(2);
    }

}