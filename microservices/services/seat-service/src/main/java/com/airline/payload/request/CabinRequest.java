package com.airline.payload.request;

import com.airline.enums.CabinClass;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CabinRequest {

    @NotNull(message = "Flight instance ID is mandatory")
    private Long flightInstanceId;

    @NotNull(message = "Cabin class is mandatory")
    private CabinClass cabinClass;

    @NotNull(message = "Row start is mandatory")
    @Min(value = 1, message = "Row start must be at least 1")
    private Integer rowStart;

    @NotNull(message = "Row end is mandatory")
    @Min(value = 1, message = "Row end must be at least 1")
    private Integer rowEnd;

    @NotBlank(message = "Column layout is mandatory")
    @Size(max = 20, message = "Column layout cannot exceed 20 characters")
    @Pattern(
            regexp = "^[A-Z]+(-[A-Z]+)*$",
            message = "Column layout must contain only uppercase letters and hyphens (e.g., ABC-DEF)"
    )
    private String columnLayout;

    @NotNull(message = "Base price is mandatory")
    @DecimalMin(value = "0.0", message = "Base price cannot be negative")
    private BigDecimal basePrice;

}