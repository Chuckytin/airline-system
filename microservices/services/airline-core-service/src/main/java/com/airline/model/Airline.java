package com.airline.model;

import com.airline.embeddable.Support;
import com.airline.enums.AirlineStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "airlines",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_airline_iata_code", columnNames = "iataCode"),
                @UniqueConstraint(name = "uk_airline_icao_code", columnNames = "icaoCode")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Airline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 3)
    private String iataCode;

    @Column(nullable = false, length = 4)
    private String icaoCode;

    @Column(nullable = false)
    private String name;

    private String alias;

    private String logoUrl;

    private String website;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AirlineStatus status = AirlineStatus.ACTIVE;

    private String alliance;

    @Embedded
    private Support support;

    @Column(nullable = false)
    private Long ownerId;

    private Long headquartersCityId;

    private Long updatedById;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

}
