package com.airline.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "cities",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_city_country",
                        columnNames = {"cityCode", "countryCode"} // La combinación de ambos debe ser única
                )
        }
)
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Size(min = 3, max = 3)
    @Column(nullable = false, length = 3)
    private String cityCode;

    @Size(min = 2, max = 3)
    @Column(nullable = false, length = 3)
    private String countryCode;

    @Column(nullable = false)
    private String countryName;

    @Size(max = 10)
    private String regionCode;

    @Column(length = 10)
    private String timeZoneOffset;

}
