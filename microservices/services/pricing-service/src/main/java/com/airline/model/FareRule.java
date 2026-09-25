package com.airline.model;

import com.airline.enums.FareRuleType;
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
        name = "fare_rules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fare_rule_type",
                        columnNames = {"fare_id", "rule_type"}
                )
        }
)
@EntityListeners(AuditingEntityListener.class)
public class FareRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fare_id", nullable = false)
    @JsonIgnore
    private Fare fare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FareRuleType ruleType;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean allowed = true;

    @Column(precision = 10, scale = 2)
    private BigDecimal penaltyAmount;

    @Column(length = 3)
    private String penaltyCurrency;

    @Column(length = 500)
    private String conditions;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

}