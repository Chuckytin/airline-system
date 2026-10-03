package com.airline.repository;

import com.airline.enums.CabinClass;
import com.airline.model.Cabin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CabinRepository extends JpaRepository<Cabin, Long> {

    Optional<Cabin> findByFlightInstanceIdAndCabinClass(
            Long flightInstanceId, CabinClass cabinClass);

    boolean existsByFlightInstanceIdAndCabinClass(
            Long flightInstanceId, CabinClass cabinClass);

    List<Cabin> findByFlightInstanceId(Long flightInstanceId);

    Page<Cabin> findByFlightInstanceId(Long flightInstanceId, Pageable pageable);

    List<Cabin> findByFlightInstanceIdAndActiveTrue(Long flightInstanceId);

    Page<Cabin> findByCabinClass(CabinClass cabinClass, Pageable pageable);

    /**
     * Busca una cabin por ID cargando sus seatMaps y seatInstances.
     */
    @Query("""
            SELECT DISTINCT c FROM Cabin c
            LEFT JOIN FETCH c.seatMaps
            LEFT JOIN FETCH c.seatInstances
            WHERE c.id = :id
            """)
    Optional<Cabin> findByIdWithDetails(@Param("id") Long id);

    /**
     * Busca cabinas por flightInstanceId cargando sus seatMaps y seatInstances.
     */
    @Query("""
            SELECT DISTINCT c FROM Cabin c
            LEFT JOIN FETCH c.seatMaps
            LEFT JOIN FETCH c.seatInstances
            WHERE c.flightInstanceId = :flightInstanceId
            """)
    List<Cabin> findByFlightInstanceIdWithDetails(@Param("flightInstanceId") Long flightInstanceId);

}