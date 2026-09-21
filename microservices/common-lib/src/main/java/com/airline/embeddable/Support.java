package com.airline.embeddable;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Support {

    @Email(message = "Support email must be valid")
    @Size(max = 100, message = "Support email cannot exceed 100 characters")
    private String email;

    @Size(max = 20, message = "Support phone cannot exceed 20 characters")
    private String phoneNumber;

    @Size(max = 50, message = "Support hours cannot exceed 50 characters")
    private String hours;

}
