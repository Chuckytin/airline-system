package com.airline.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuración global para habilitar la auditoría automática de JPA.
 * Permite registrar automáticamente las fechas y usuarios de creación y modificación.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
