package com.airline.payload.response;

import com.airline.enums.FareRuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareRuleResponse {

    private Long id;
    private Long fareId;
    private FareRuleType ruleType;
    private String description;
    private Boolean allowed;
    private BigDecimal penaltyAmount;
    private String penaltyCurrency;
    private String conditions;

    private Instant createdAt;
    private Instant updatedAt;

}