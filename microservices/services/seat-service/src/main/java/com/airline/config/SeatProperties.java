package com.airline.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.seat")
public class SeatProperties {

    private String defaultCurrency = "EUR";

    private BigDecimal defaultPriceModifier = BigDecimal.ZERO;

    private Boolean defaultActive = true;

    /**
     * Prefijo de fila a partir del cual se considera fila de emergencia.
     * 0 = ninguna fila es de emergencia por defecto (se marcará manualmente).
     */
    private Integer exitRowStart = 0;

    private Integer exitRowEnd = 0;

}