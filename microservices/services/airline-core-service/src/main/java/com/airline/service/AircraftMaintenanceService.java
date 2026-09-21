package com.airline.service;

import com.airline.model.Aircraft;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Servicio encargado de las validaciones y cálculos relacionados con el mantenimiento de aeronaves.
 */
@Slf4j
@Service
public class AircraftMaintenanceService {

    private final int maintenanceAlertWeeks;

    public AircraftMaintenanceService(
            @Value("${app.aircraft.maintenance.alert-weeks:2}") int maintenanceAlertWeeks
    ) {
        this.maintenanceAlertWeeks = maintenanceAlertWeeks;
    }

    /**
     * Determina si una aeronave requiere mantenimiento próximo.
     */
    public boolean requiresMaintenance(Aircraft aircraft) {
        if (aircraft == null || aircraft.getNextMaintenanceDate() == null) {
            return false;
        }

        LocalDate threshold = LocalDate.now().plusWeeks(maintenanceAlertWeeks);
        return aircraft.getNextMaintenanceDate().isBefore(threshold);
    }

}