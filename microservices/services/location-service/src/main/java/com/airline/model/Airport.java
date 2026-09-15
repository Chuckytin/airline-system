package com.airline.model;

import com.airline.embeddable.Address;
import com.airline.embeddable.GeoCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "airports",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_airport_iata", columnNames = "iataCode")
        }
)
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 3)
    private String iataCode;

    @Column(nullable = false)
    private String name;

    @Embedded
    private Address address;

    @Embedded
    private GeoCode geoCode;

    @Column(length = 50)
    private String timeZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private City city;

}
