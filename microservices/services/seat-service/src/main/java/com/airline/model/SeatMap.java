package com.airline.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "seat_maps",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_seat_map_cabin_row",
                        columnNames = {"cabin_id", "seat_row"}
                )
        }
)
@EntityListeners(AuditingEntityListener.class)
public class SeatMap {

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

    @Column(nullable = false)
    private Integer seatRow;

    /**
     * Letras de asiento en esta fila (ej: "ABC", "DEF").
     */
    @Column(nullable = false, length = 10)
    private String seatLetters;

    /**
     * Si la fila tiene más espacio para las piernas.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean hasExtraLegroom = false;

    /**
     * Si la fila es de salida de emergencia.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isExitRow = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

}