package com.airline.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.pricing")
public class PricingProperties {

    private String defaultCurrency = "EUR";

    private BaggageDefaults baggage = new BaggageDefaults();

    @Getter
    @Setter
    public static class BaggageDefaults {

        private Integer carryOnPieces = 1;
        private Integer carryOnWeightKg = 7;
        private Integer checkedPieces = 0;
        private Integer checkedWeightKg = 0;
        private BigDecimal extraBagPrice = BigDecimal.ZERO;
        
    }

}