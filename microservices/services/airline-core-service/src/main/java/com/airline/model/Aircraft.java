package com.airline.model;

import com.airline.enums.AircraftStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "aircraft",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_aircraft_code", columnNames = "code")
        }
)
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false, length = 50)
    private String manufacturer;

    @Column(nullable = false)
    @Builder.Default
    private Integer economySeats = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer premiumEconomySeats = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer businessSeats = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer firstClassSeats = 0;

    private Integer rangeKm;

    private Integer cruisingSpeedKmh;

    private Integer maxAltitudeFt;

    private Integer yearOfManufacture;

    private LocalDate registrationDate;

    private LocalDate nextMaintenanceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AircraftStatus status = AircraftStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean available = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airline_id")
    @JsonIgnore
    private Airline airline;

    private Long currentAirportId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @JsonIgnore
    @Transient
    public Integer getTotalSeats() {
        return economySeats + premiumEconomySeats + businessSeats + firstClassSeats;
    }

    @JsonIgnore
    @Transient
    public boolean isOperational() {
        return AircraftStatus.ACTIVE.equals(status)
                && Boolean.TRUE.equals(available);
    }

}