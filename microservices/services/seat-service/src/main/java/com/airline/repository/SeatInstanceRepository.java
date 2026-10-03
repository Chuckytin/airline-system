package com.airline.repository;

import com.airline.enums.SeatStatus;
import com.airline.enums.SeatType;
import com.airline.model.SeatInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatInstanceRepository extends JpaRepository<SeatInstance, Long> {

    List<SeatInstance> findByCabinId(Long cabinId);

    List<SeatInstance> findByCabinIdOrderBySeatRowAscColumnLetterAsc(Long cabinId);

    Page<SeatInstance> findByCabinId(Long cabinId, Pageable pageable);

    List<SeatInstance> findByCabinIdAndStatus(Long cabinId, SeatStatus status);

    long countByCabinIdAndStatus(Long cabinId, SeatStatus status);

    List<SeatInstance> findByCabinIdAndSeatType(Long cabinId, SeatType seatType);

    Optional<SeatInstance> findByCabinIdAndSeatNumber(Long cabinId, String seatNumber);

    boolean existsByCabinIdAndSeatNumber(Long cabinId, String seatNumber);

    List<SeatInstance> findByBookingId(Long bookingId);

    List<SeatInstance> findByPassengerId(Long passengerId);

    /**
     * Busca un seatInstance por ID cargando su cabin.
     */
    @Query("""
            SELECT si FROM SeatInstance si
            JOIN FETCH si.cabin
            WHERE si.id = :id
            """)
    Optional<SeatInstance> findByIdWithCabin(@Param("id") Long id);

    /**
     * Busca seatInstances por cabinId cargando la cabin.
     */
    @Query("""
            SELECT si FROM SeatInstance si
            JOIN FETCH si.cabin
            WHERE si.cabin.id = :cabinId
            ORDER BY si.seatRow ASC, si.columnLetter ASC
            """)
    List<SeatInstance> findByCabinIdWithCabin(@Param("cabinId") Long cabinId);

    /**
     * Cuenta los asientos disponibles por cabin.
     */
    @Query("""
            SELECT COUNT(si) FROM SeatInstance si
            WHERE si.cabin.id = :cabinId
              AND si.status = :status
            """)
    long countByCabinIdAndStatusQuery(
            @Param("cabinId") Long cabinId,
            @Param("status") SeatStatus status);

    /**
     * Busca los asientos disponibles de una cabin (para reservas).
     */
    @Query("""
            SELECT si FROM SeatInstance si
            WHERE si.cabin.id = :cabinId
              AND si.status = :status
            ORDER BY si.seatRow ASC, si.columnLetter ASC
            """)
    List<SeatInstance> findAvailableSeatsByCabinId(
            @Param("cabinId") Long cabinId,
            @Param("status") SeatStatus status);

    @Query("""
            SELECT si.status, COUNT(si)
            FROM SeatInstance si
            WHERE si.cabin.id = :cabinId
            GROUP BY si.status
            """)
    List<Object[]> countByCabinIdGroupedByStatus(@Param("cabinId") Long cabinId);

    boolean existsByCabinIdAndBookingIdIsNotNull(Long cabinId);

    boolean existsByCabinIdAndSeatRowAndBookingIdIsNotNull(Long cabinId, Integer seatRow);

}