package com.airline.repository;

import com.airline.config.JpaAuditingConfig;
import com.airline.enums.CabinClass;
import com.airline.model.Cabin;
import com.airline.model.SeatMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("SeatMapRepository Tests")
class SeatMapRepositoryTest {

    @Autowired
    private CabinRepository cabinRepository;

    @Autowired
    private SeatMapRepository seatMapRepository;

    private Cabin cabin;

    @BeforeEach
    void setUp() {
        cabin = cabinRepository.save(Cabin.builder()
                .flightInstanceId(1L)
                .cabinClass(CabinClass.ECONOMY)
                .rowStart(1).rowEnd(3)
                .columnLayout("ABC-DEF")
                .totalSeats(18)
                .availableSeats(18)
                .basePrice(new BigDecimal("50.00"))
                .currency("EUR")
                .active(true)
                .build());
    }

    @Test
    @DisplayName("Should save and find by ID")
    void shouldSaveAndFindById() {
        SeatMap seatMap = seatMapRepository.save(buildSeatMap(1, "ABC"));

        Optional<SeatMap> found = seatMapRepository.findById(seatMap.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSeatRow()).isEqualTo(1);
        assertThat(found.get().getSeatLetters()).isEqualTo("ABC");
    }

    @Test
    @DisplayName("Should find by cabinId")
    void shouldFindByCabinId() {
        seatMapRepository.save(buildSeatMap(1, "ABC"));
        seatMapRepository.save(buildSeatMap(2, "ABC"));
        seatMapRepository.save(buildSeatMap(3, "ABC"));

        List<SeatMap> maps = seatMapRepository.findByCabinId(cabin.getId());

        assertThat(maps).hasSize(3);
    }

    @Test
    @DisplayName("Should find by cabinId ordered by seatRow")
    void shouldFindByCabinIdOrdered() {
        seatMapRepository.save(buildSeatMap(3, "ABC"));
        seatMapRepository.save(buildSeatMap(1, "ABC"));
        seatMapRepository.save(buildSeatMap(2, "ABC"));

        List<SeatMap> maps = seatMapRepository.findByCabinIdOrderBySeatRowAsc(cabin.getId());

        assertThat(maps).hasSize(3);
        assertThat(maps.get(0).getSeatRow()).isEqualTo(1);
        assertThat(maps.get(1).getSeatRow()).isEqualTo(2);
        assertThat(maps.get(2).getSeatRow()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find by cabinId and seatRow")
    void shouldFindByCabinIdAndSeatRow() {
        seatMapRepository.save(buildSeatMap(1, "ABC"));

        Optional<SeatMap> found = seatMapRepository.findByCabinIdAndSeatRow(
                cabin.getId(), 1);

        assertThat(found).isPresent();
        assertThat(found.get().getSeatLetters()).isEqualTo("ABC");
    }

    @Test
    @DisplayName("Should check exists by cabinId and seatRow")
    void shouldCheckExists() {
        seatMapRepository.save(buildSeatMap(1, "ABC"));

        assertThat(seatMapRepository.existsByCabinIdAndSeatRow(cabin.getId(), 1)).isTrue();
        assertThat(seatMapRepository.existsByCabinIdAndSeatRow(cabin.getId(), 99)).isFalse();
    }

    @Test
    @DisplayName("Should find by ID with cabin fetched")
    void shouldFindByIdWithCabin() {
        SeatMap seatMap = seatMapRepository.save(buildSeatMap(1, "ABC"));

        Optional<SeatMap> found = seatMapRepository.findByIdWithCabin(seatMap.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCabin().getId()).isEqualTo(cabin.getId());
    }

    @Test
    @DisplayName("Should find by cabinId with cabin fetched")
    void shouldFindByCabinIdWithCabin() {
        seatMapRepository.save(buildSeatMap(1, "ABC"));
        seatMapRepository.save(buildSeatMap(2, "ABC"));

        List<SeatMap> maps = seatMapRepository.findByCabinIdWithCabin(cabin.getId());

        assertThat(maps).hasSize(2);
        assertThat(maps).allMatch(m -> m.getCabin().getId().equals(cabin.getId()));
    }

    @Test
    @DisplayName("Should enforce unique constraint on cabinId + seatRow")
    void shouldEnforceUniqueConstraint() {
        seatMapRepository.save(buildSeatMap(1, "ABC"));

        SeatMap duplicate = buildSeatMap(1, "DEF");

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> seatMapRepository.saveAndFlush(duplicate))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    private SeatMap buildSeatMap(int row, String letters) {
        return SeatMap.builder()
                .cabin(cabin)
                .seatRow(row)
                .seatLetters(letters)
                .hasExtraLegroom(false)
                .isExitRow(false)
                .build();
    }
}