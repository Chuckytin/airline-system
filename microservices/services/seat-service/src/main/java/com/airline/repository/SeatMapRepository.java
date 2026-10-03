package com.airline.repository;

import com.airline.model.SeatMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatMapRepository extends JpaRepository<SeatMap, Long> {

    List<SeatMap> findByCabinId(Long cabinId);

    List<SeatMap> findByCabinIdOrderBySeatRowAsc(Long cabinId);

    Optional<SeatMap> findByCabinIdAndSeatRow(Long cabinId, Integer seatRow);

    boolean existsByCabinIdAndSeatRow(Long cabinId, Integer seatRow);

    /**
     * Busca un seatMap por ID cargando su cabin.
     */
    @Query("""
            SELECT sm FROM SeatMap sm
            JOIN FETCH sm.cabin
            WHERE sm.id = :id
            """)
    Optional<SeatMap> findByIdWithCabin(@Param("id") Long id);

    /**
     * Busca seatMaps por cabinId cargando la cabin.
     */
    @Query("""
            SELECT sm FROM SeatMap sm
            JOIN FETCH sm.cabin
            WHERE sm.cabin.id = :cabinId
            ORDER BY sm.seatRow ASC
            """)
    List<SeatMap> findByCabinIdWithCabin(@Param("cabinId") Long cabinId);

}