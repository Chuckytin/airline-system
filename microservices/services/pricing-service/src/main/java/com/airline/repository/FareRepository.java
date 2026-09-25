package com.airline.repository;

import com.airline.enums.CabinClass;
import com.airline.enums.FareType;
import com.airline.model.Fare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FareRepository extends JpaRepository<Fare, Long> {

    boolean existsByFlightIdAndCabinClassAndFareType(
            Long flightId, CabinClass cabinClass, FareType fareType);

    Optional<Fare> findByFlightIdAndCabinClassAndFareType(
            Long flightId, CabinClass cabinClass, FareType fareType);

    List<Fare> findByFlightId(Long flightId);

    List<Fare> findByFlightIdAndActiveTrue(Long flightId);

    Page<Fare> findByActiveTrue(Pageable pageable);

    @Query("""
            SELECT f FROM Fare f
            WHERE f.flightId = :flightId
              AND f.active = true
              AND f.validFrom <= :date
              AND (f.validUntil IS NULL OR f.validUntil >= :date)
            """)
    List<Fare> findActiveFaresForDate(
            @Param("flightId") Long flightId,
            @Param("date") LocalDate date);

}