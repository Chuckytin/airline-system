package com.airline.model;

import com.airline.enums.CabinClass;
import com.airline.enums.FareType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "fares",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fare_flight_cabin_type",
                        columnNames = {"flight_id", "cabin_class", "fare_type"}
                )
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Fare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long flightId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CabinClass cabinClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FareType fareType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxes;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private LocalDate validFrom;

    private LocalDate validUntil;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Transient
    public BigDecimal getTotalPrice() {
        BigDecimal base = basePrice != null ? basePrice : BigDecimal.ZERO;
        BigDecimal tax = taxes != null ? taxes : BigDecimal.ZERO;
        return base.add(tax);
    }

}