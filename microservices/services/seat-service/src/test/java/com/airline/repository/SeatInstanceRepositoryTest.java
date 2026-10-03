package com.airline.repository;

import com.airline.config.JpaAuditingConfig;
import com.airline.enums.CabinClass;
import com.airline.enums.SeatStatus;
import com.airline.enums.SeatType;
import com.airline.model.Cabin;
import com.airline.model.SeatInstance;
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
@DisplayName("SeatInstanceRepository Tests")
class SeatInstanceRepositoryTest {

    @Autowired
    private CabinRepository cabinRepository;

    @Autowired
    private SeatInstanceRepository seatInstanceRepository;

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
        SeatInstance seat = seatInstanceRepository.save(buildSeat("1A", 1, "A"));

        Optional<SeatInstance> found = seatInstanceRepository.findById(seat.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSeatNumber()).isEqualTo("1A");
    }

    @Test
    @DisplayName("Should find by cabinId ordered")
    void shouldFindByCabinIdOrdered() {
        seatInstanceRepository.save(buildSeat("2A", 2, "A"));
        seatInstanceRepository.save(buildSeat("1B", 1, "B"));
        seatInstanceRepository.save(buildSeat("1A", 1, "A"));

        List<SeatInstance> seats =
                seatInstanceRepository.findByCabinIdOrderBySeatRowAscColumnLetterAsc(cabin.getId());

        assertThat(seats).hasSize(3);
        assertThat(seats.get(0).getSeatNumber()).isEqualTo("1A");
        assertThat(seats.get(1).getSeatNumber()).isEqualTo("1B");
        assertThat(seats.get(2).getSeatNumber()).isEqualTo("2A");
    }

    @Test
    @DisplayName("Should find by cabinId and status")
    void shouldFindByCabinIdAndStatus() {
        SeatInstance a = buildSeat("1A", 1, "A");
        SeatInstance b = buildSeat("1B", 1, "B");
        b.setStatus(SeatStatus.OCCUPIED);
        b.setBookingId(100L);
        b.setPassengerId(200L);

        seatInstanceRepository.save(a);
        seatInstanceRepository.save(b);

        List<SeatInstance> occupied = seatInstanceRepository
                .findByCabinIdAndStatus(cabin.getId(), SeatStatus.OCCUPIED);

        assertThat(occupied).hasSize(1);
        assertThat(occupied.getFirst().getSeatNumber()).isEqualTo("1B");
    }

    @Test
    @DisplayName("Should count by cabinId and status")
    void shouldCountByCabinIdAndStatus() {
        SeatInstance a = buildSeat("1A", 1, "A");
        SeatInstance b = buildSeat("1B", 1, "B");
        b.setStatus(SeatStatus.OCCUPIED);

        seatInstanceRepository.save(a);
        seatInstanceRepository.save(b);

        long available = seatInstanceRepository
                .countByCabinIdAndStatus(cabin.getId(), SeatStatus.AVAILABLE);
        long occupied = seatInstanceRepository
                .countByCabinIdAndStatus(cabin.getId(), SeatStatus.OCCUPIED);

        assertThat(available).isEqualTo(1);
        assertThat(occupied).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find by cabinId and seatType")
    void shouldFindByCabinIdAndSeatType() {
        SeatInstance window = buildSeat("1A", 1, "A");
        window.setSeatType(SeatType.WINDOW);

        SeatInstance middle = buildSeat("1B", 1, "B");
        middle.setSeatType(SeatType.MIDDLE);

        seatInstanceRepository.save(window);
        seatInstanceRepository.save(middle);

        List<SeatInstance> windows = seatInstanceRepository
                .findByCabinIdAndSeatType(cabin.getId(), SeatType.WINDOW);

        assertThat(windows).hasSize(1);
    }

    @Test
    @DisplayName("Should find by cabinId and seatNumber")
    void shouldFindByCabinIdAndSeatNumber() {
        seatInstanceRepository.save(buildSeat("1A", 1, "A"));

        Optional<SeatInstance> found = seatInstanceRepository
                .findByCabinIdAndSeatNumber(cabin.getId(), "1A");

        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("Should check exists by cabinId and seatNumber")
    void shouldCheckExists() {
        seatInstanceRepository.save(buildSeat("1A", 1, "A"));

        assertThat(seatInstanceRepository
                .existsByCabinIdAndSeatNumber(cabin.getId(), "1A")).isTrue();
        assertThat(seatInstanceRepository
                .existsByCabinIdAndSeatNumber(cabin.getId(), "99Z")).isFalse();
    }

    @Test
    @DisplayName("Should find by bookingId")
    void shouldFindByBookingId() {
        SeatInstance a = buildSeat("1A", 1, "A");
        a.setBookingId(100L);
        SeatInstance b = buildSeat("1B", 1, "B");
        b.setBookingId(100L);

        seatInstanceRepository.save(a);
        seatInstanceRepository.save(b);

        List<SeatInstance> seats = seatInstanceRepository.findByBookingId(100L);

        assertThat(seats).hasSize(2);
    }

    @Test
    @DisplayName("Should find by passengerId")
    void shouldFindByPassengerId() {
        SeatInstance a = buildSeat("1A", 1, "A");
        a.setPassengerId(200L);
        seatInstanceRepository.save(a);

        List<SeatInstance> seats = seatInstanceRepository.findByPassengerId(200L);

        assertThat(seats).hasSize(1);
    }

    @Test
    @DisplayName("Should find by ID with cabin fetched")
    void shouldFindByIdWithCabin() {
        SeatInstance seat = seatInstanceRepository.save(buildSeat("1A", 1, "A"));

        Optional<SeatInstance> found = seatInstanceRepository.findByIdWithCabin(seat.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCabin().getId()).isEqualTo(cabin.getId());
    }

    @Test
    @DisplayName("Should find by cabinId with cabin fetched ordered")
    void shouldFindByCabinIdWithCabin() {
        seatInstanceRepository.save(buildSeat("2A", 2, "A"));
        seatInstanceRepository.save(buildSeat("1B", 1, "B"));
        seatInstanceRepository.save(buildSeat("1A", 1, "A"));

        List<SeatInstance> seats =
                seatInstanceRepository.findByCabinIdWithCabin(cabin.getId());

        assertThat(seats).hasSize(3);
        assertThat(seats.get(0).getSeatNumber()).isEqualTo("1A");
        assertThat(seats.get(2).getSeatNumber()).isEqualTo("2A");
    }

    @Test
    @DisplayName("Should paginate by cabinId")
    void shouldPaginateByCabinId() {
        seatInstanceRepository.save(buildSeat("1A", 1, "A"));
        seatInstanceRepository.save(buildSeat("1B", 1, "B"));

        Page<SeatInstance> page = seatInstanceRepository.findByCabinId(
                cabin.getId(), PageRequest.of(0, 1));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should find available seats ordered")
    void shouldFindAvailableSeats() {
        SeatInstance a = buildSeat("1A", 1, "A");
        SeatInstance b = buildSeat("1B", 1, "B");
        b.setStatus(SeatStatus.OCCUPIED);
        SeatInstance c = buildSeat("2A", 2, "A");

        seatInstanceRepository.save(a);
        seatInstanceRepository.save(b);
        seatInstanceRepository.save(c);

        List<SeatInstance> available = seatInstanceRepository
                .findAvailableSeatsByCabinId(cabin.getId(), SeatStatus.AVAILABLE);

        assertThat(available).hasSize(2);
        assertThat(available.get(0).getSeatNumber()).isEqualTo("1A");
        assertThat(available.get(1).getSeatNumber()).isEqualTo("2A");
    }

    @Test
    @DisplayName("Should count grouped by status")
    void shouldCountGroupedByStatus() {
        SeatInstance a = buildSeat("1A", 1, "A");
        SeatInstance b = buildSeat("1B", 1, "B");
        SeatInstance c = buildSeat("1C", 1, "C");
        SeatInstance d = buildSeat("2A", 2, "A");

        b.setStatus(SeatStatus.OCCUPIED);
        c.setStatus(SeatStatus.OCCUPIED);
        d.setStatus(SeatStatus.BLOCKED);

        seatInstanceRepository.saveAll(List.of(a, b, c, d));

        List<Object[]> counts = seatInstanceRepository
                .countByCabinIdGroupedByStatus(cabin.getId());

        assertThat(counts).hasSize(3);   // AVAILABLE, OCCUPIED, BLOCKED

        long availableCount = extractCount(counts, SeatStatus.AVAILABLE);
        long occupiedCount = extractCount(counts, SeatStatus.OCCUPIED);
        long blockedCount = extractCount(counts, SeatStatus.BLOCKED);

        assertThat(availableCount).isEqualTo(1);
        assertThat(occupiedCount).isEqualTo(2);
        assertThat(blockedCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should check exists by cabinId with non-null bookingId")
    void shouldCheckExistsWithBookingId() {
        SeatInstance a = buildSeat("1A", 1, "A");
        a.setBookingId(100L);
        seatInstanceRepository.save(a);

        assertThat(seatInstanceRepository
                .existsByCabinIdAndBookingIdIsNotNull(cabin.getId())).isTrue();
    }

    @Test
    @DisplayName("Should check exists by cabinId + seatRow with non-null bookingId")
    void shouldCheckExistsByRowWithBookingId() {
        SeatInstance a = buildSeat("1A", 1, "A");
        a.setBookingId(100L);
        seatInstanceRepository.save(a);

        assertThat(seatInstanceRepository
                .existsByCabinIdAndSeatRowAndBookingIdIsNotNull(cabin.getId(), 1)).isTrue();
        assertThat(seatInstanceRepository
                .existsByCabinIdAndSeatRowAndBookingIdIsNotNull(cabin.getId(), 2)).isFalse();
    }

    private SeatInstance buildSeat(String number, int row, String letter) {
        return SeatInstance.builder()
                .cabin(cabin)
                .seatNumber(number)
                .seatRow(row)
                .columnLetter(letter)
                .seatType(SeatType.MIDDLE)
                .status(SeatStatus.AVAILABLE)
                .hasExtraLegroom(false)
                .isExitRow(false)
                .priceModifier(BigDecimal.ZERO)
                .build();
    }

    private long extractCount(List<Object[]> counts, SeatStatus status) {
        return counts.stream()
                .filter(row -> row[0] == status)
                .mapToLong(row -> ((Number) row[1]).longValue())
                .findFirst()
                .orElse(0L);
    }
}