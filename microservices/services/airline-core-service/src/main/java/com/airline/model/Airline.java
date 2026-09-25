package com.airline.model;

import com.airline.embeddable.Support;
import com.airline.enums.AirlineStatus;
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
        name = "airlines",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_airline_iata_code", columnNames = "iata_code"),
                @UniqueConstraint(name = "uk_airline_icao_code", columnNames = "icao_code")
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

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

}
