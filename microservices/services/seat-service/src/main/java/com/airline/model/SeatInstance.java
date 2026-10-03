package com.airline.model;

import com.airline.enums.SeatStatus;
import com.airline.enums.SeatType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "seat_instances",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_seat_instance_cabin_number",
                        columnNames = {"cabin_id", "seat_number"}
                )
        }
)
@EntityListeners(AuditingEntityListener.class)
public class SeatInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relación con Cabin (mismo microservicio).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cabin_id", nullable = false)
    @JsonIgnore
    private Cabin cabin;

    /**
     * Número de asiento (ej: "12A").
     */
    @Column(nullable = false, length = 5)
    private String seatNumber;

    @Column(nullable = false)
    private Integer seatRow;

    @Column(nullable = false, length = 1)
    private String columnLetter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatType seatType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean hasExtraLegroom = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isExitRow = false;

    /**
     * Suplemento por tipo de asiento (ventana, emergencia, etc.).
     */
    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal priceModifier = BigDecimal.ZERO;

    /**
     * Referencia externa a Booking (booking-service).
     */
    private Long bookingId;

    /**
     * Referencia externa a Passenger (booking-service).
     */
    private Long passengerId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

}