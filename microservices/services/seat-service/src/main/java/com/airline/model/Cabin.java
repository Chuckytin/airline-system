package com.airline.model;

import com.airline.enums.CabinClass;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "cabins",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cabin_flight_class",
                        columnNames = {"flight_instance_id", "cabin_class"}
                )
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Cabin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Referencia externa a FlightInstance (flight-ops-service).
     */
    @Column(nullable = false)
    private Long flightInstanceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CabinClass cabinClass;

    @Column(nullable = false)
    private Integer rowStart;

    @Column(nullable = false)
    private Integer rowEnd;

    /**
     * Layout de columnas (ej: "ABC-DEF", "AB-CD").
     * El guion "-" representa el pasillo.
     */
    @Column(nullable = false, length = 20)
    private String columnLayout;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Integer availableSeats;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "EUR";

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "cabin", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("seatRow ASC")
    @Builder.Default
    private Set<SeatMap> seatMaps = new LinkedHashSet<>();

    @OneToMany(mappedBy = "cabin", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("seatRow ASC, columnLetter ASC")
    @Builder.Default
    private Set<SeatInstance> seatInstances = new LinkedHashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

}