package com.airline.repository;

import com.airline.model.FlightSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FlightScheduleRepository extends JpaRepository<FlightSchedule, Long> {

    /**
     * Busca un schedule por ID cargando el flight y los días (JOIN FETCH).
     */
    @Query("""
            SELECT fs FROM FlightSchedule fs
            JOIN FETCH fs.flight
            LEFT JOIN FETCH fs.operatingDays
            WHERE fs.id = :id
            """)
    Optional<FlightSchedule> findByIdWithDetails(@Param("id") Long id);

    /**
     * Busca schedules por flightId cargando los días (JOIN FETCH + DISTINCT).
     */
    @Query("""
            SELECT DISTINCT fs FROM FlightSchedule fs
            LEFT JOIN FETCH fs.operatingDays
            WHERE fs.flight.id = :flightId
            """)
    List<FlightSchedule> findByFlightIdWithDays(@Param("flightId") Long flightId);

}