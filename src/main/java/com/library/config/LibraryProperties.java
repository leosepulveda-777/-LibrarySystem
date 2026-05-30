package com.library.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.library")
@Getter
@Setter
public class LibraryProperties {
    private int maxRenewals = 2;
    private int loanDaysPhysical = 15;       // US-011: 15 días (era 14)
    private int loanDaysDigital = 21;        // US-012: 21 días (era 7)
    private double finePerDay = 1000.0;      // US-019: $1000/día (era 500)
    private int maxActiveLoans = 5;          // US-011: máx 5 físicos (era 3)
    private int maxActiveLoansPhysical = 5;  // US-011
    private int maxActiveLoansDigital = 3;   // US-012
    private int maxActiveReservations = 3;   // US-016: máx 3 (era 2)
    private int reservationExpiryDays = 3;
}