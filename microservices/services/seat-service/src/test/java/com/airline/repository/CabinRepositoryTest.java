package com.airline.repository;

import com.airline.config.JpaAuditingConfig;
import com.airline.enums.CabinClass;
import com.airline.enums.SeatStatus;
import com.airline.enums.SeatType;
import com.airline.model.Cabin;
import com.airline.model.SeatInstance;
import com.airline.model.SeatMap;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("CabinRepository Tests")
class CabinRepositoryTest {

    @Autowired
    private CabinRepository cabinRepository;

    @Autowired
    private SeatMapRepository seatMapRepository;

    @Autowired
    private SeatInstanceRepository seatInstanceRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Cabin economy;
    private Cabin business;

    @BeforeEach
    void setUp() {
        economy = Cabin.builder()
                .flightInstanceId(1L)
                .cabinClass(CabinClass.ECONOMY)
                .rowStart(1).rowEnd(10)
                .columnLayout("ABC-DEF")
                .totalSeats(60)
                .availableSeats(60)
                .basePrice(new BigDecimal("50.00"))
                .currency("EUR")
                .active(true)
                .build();

        business = Cabin.builder()
                .flightInstanceId(1L)
                .cabinClass(CabinClass.BUSINESS)
                .rowStart(1).rowEnd(4)
                .columnLayout("AB-CD")
                .totalSeats(16)
                .availableSeats(16)
                .basePrice(new BigDecimal("200.00"))
                .currency("EUR")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should save and find cabin by ID")
    void shouldSaveAndFindById() {
        Cabin saved = cabinRepository.save(economy);

        Optional<Cabin> found = cabinRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCabinClass()).isEqualTo(CabinClass.ECONOMY);
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find by flightInstanceId + cabinClass")
    void shouldFindByFlightAndClass() {
        cabinRepository.save(economy);

        Optional<Cabin> found = cabinRepository.findByFlightInstanceIdAndCabinClass(
                1L, CabinClass.ECONOMY);

        assertThat(found).isPresent();
        assertThat(found.get().getTotalSeats()).isEqualTo(60);
    }

    @Test
    @DisplayName("Should return empty when not found")
    void shouldReturnEmptyWhenNotFound() {
        Optional<Cabin> found = cabinRepository.findByFlightInstanceIdAndCabinClass(
                999L, CabinClass.ECONOMY);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check exists by flightInstanceId + cabinClass")
    void shouldCheckExists() {
        cabinRepository.save(economy);

        assertThat(cabinRepository.existsByFlightInstanceIdAndCabinClass(
                1L, CabinClass.ECONOMY)).isTrue();
        assertThat(cabinRepository.existsByFlightInstanceIdAndCabinClass(
                1L, CabinClass.FIRST)).isFalse();
    }

    @Test
    @DisplayName("Should find all cabins by flightInstanceId")
    void shouldFindByFlightInstanceId() {
        cabinRepository.save(economy);
        cabinRepository.save(business);

        List<Cabin> cabins = cabinRepository.findByFlightInstanceId(1L);

        assertThat(cabins).hasSize(2);
    }

    @Test
    @DisplayName("Should paginate cabins by flightInstanceId")
    void shouldPaginateByFlightInstanceId() {
        cabinRepository.save(economy);
        cabinRepository.save(business);

        Page<Cabin> page = cabinRepository.findByFlightInstanceId(
                1L, PageRequest.of(0, 1));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should find active cabins by flightInstanceId")
    void shouldFindActiveByFlightInstanceId() {
        cabinRepository.save(economy);
        business.setActive(false);
        cabinRepository.save(business);

        List<Cabin> actives = cabinRepository.findByFlightInstanceIdAndActiveTrue(1L);

        assertThat(actives).hasSize(1);
        assertThat(actives.getFirst().getCabinClass()).isEqualTo(CabinClass.ECONOMY);
    }

    @Test
    @DisplayName("Should paginate cabins by cabinClass")
    void shouldPaginateByCabinClass() {
        cabinRepository.save(economy);
        cabinRepository.save(business);

        Page<Cabin> page = cabinRepository.findByCabinClass(
                CabinClass.ECONOMY, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find cabin by ID with details")
    void shouldFindByIdWithDetails() {
        Cabin saved = cabinRepository.save(economy);

        seatMapRepository.save(SeatMap.builder()
                .cabin(saved).seatRow(1).seatLetters("ABC")
                .hasExtraLegroom(false).isExitRow(false).build());
        seatInstanceRepository.save(SeatInstance.builder()
                .cabin(saved).seatNumber("1A").seatRow(1).columnLetter("A")
                .seatType(SeatType.WINDOW).status(SeatStatus.AVAILABLE)
                .hasExtraLegroom(false).isExitRow(false)
                .priceModifier(BigDecimal.ZERO).build());

        entityManager.flush();
        entityManager.clear();

        Optional<Cabin> found = cabinRepository.findByIdWithDetails(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSeatMaps()).hasSize(1);
        assertThat(found.get().getSeatInstances()).hasSize(1);
    }

    @Test
    @DisplayName("Should find cabins by flightInstanceId with details")
    void shouldFindByFlightInstanceIdWithDetails() {
        Cabin savedEconomy = cabinRepository.save(economy);
        cabinRepository.save(business);   // <-- no asignes a variable si no la usas

        seatMapRepository.save(SeatMap.builder()
                .cabin(savedEconomy).seatRow(1).seatLetters("ABC")
                .hasExtraLegroom(false).isExitRow(false).build());

        entityManager.flush();
        entityManager.clear();

        List<Cabin> cabins = cabinRepository.findByFlightInstanceIdWithDetails(1L);

        assertThat(cabins).hasSize(2);
        Cabin withSeats = cabins.stream()
                .filter(c -> c.getId().equals(savedEconomy.getId()))
                .findFirst().orElseThrow();
        assertThat(withSeats.getSeatMaps()).hasSize(1);
    }
}