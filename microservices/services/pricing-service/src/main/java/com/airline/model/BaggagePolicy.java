package com.airline.model;

import com.airline.enums.CabinClass;
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
        name = "baggage_policies",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_baggage_policy_fare",
                        columnNames = {"fare_id"}
                )
        }
)
@EntityListeners(AuditingEntityListener.class)
public class BaggagePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fare_id", nullable = false)
    @JsonIgnore
    private Fare fare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CabinClass cabinClass;

    @Column(nullable = false)
    private Integer carryOnPieces;

    @Column(nullable = false)
    private Integer carryOnWeightKg;

    @Column(nullable = false)
    private Integer checkedPieces;

    @Column(nullable = false)
    private Integer checkedWeightKg;

    @Column(precision = 10, scale = 2)
    private BigDecimal extraBagPrice;

    @Column(length = 3)
    private String currency;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

}