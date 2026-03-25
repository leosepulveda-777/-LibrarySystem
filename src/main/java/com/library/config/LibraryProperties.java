package com.library.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propiedades de configuración de la biblioteca
 */
@Configuration
@ConfigurationProperties(prefix = "application.library")
@Getter
@Setter
public class LibraryProperties {
    private int maxRenewals = 2;
    private int loanDaysPhysical = 14;
    private int loanDaysDigital = 7;
    private double finePerDay = 500.0;
    private int maxActiveLoans = 3;
    private int maxActiveReservations = 2;
    private int reservationExpiryDays = 3;
}
