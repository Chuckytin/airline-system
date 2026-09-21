package com.airline.embeddable;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {

    @NotBlank(message = "Street is mandatory")
    @Size(max = 150, message = "Street cannot exceed 150 characters")
    private String street;

    @NotBlank(message = "Postal code is mandatory")
    @Size(max = 10, message = "Postal code cannot exceed 10 characters")
    private String postalCode;

}
