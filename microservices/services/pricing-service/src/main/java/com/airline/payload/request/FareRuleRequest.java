package com.airline.payload.request;

import com.airline.enums.FareRuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareRuleRequest {

    @NotNull(message = "Fare ID is mandatory")
    private Long fareId;

    @NotNull(message = "Rule type is mandatory")
    private FareRuleType ruleType;

    @NotBlank(message = "Description is mandatory")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private Boolean allowed;

    private BigDecimal penaltyAmount;

    @Size(min = 3, max = 3, message = "Penalty currency must be 3 characters")
    private String penaltyCurrency;

    @Size(max = 500, message = "Conditions cannot exceed 500 characters")
    private String conditions;

}