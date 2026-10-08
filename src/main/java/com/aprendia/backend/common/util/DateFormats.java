package com.aprendia.backend.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/** Formatos de fecha de la Especificación v2.0: fechas "YYYY-MM-DD" e instantes ISO-8601 UTC ("2024-01-10T09:00:00Z"). */
public final class DateFormats {

    private DateFormats() {
    }

    /** LocalDateTime de la BD (hora del servidor) -> "2024-01-10T15:00:00Z". */
    public static String isoInstant(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZone(ZoneId.systemDefault()).toInstant().truncatedTo(ChronoUnit.SECONDS).toString();
    }

    /** persons.birth_date es TIMESTAMP en la BD; la API solo maneja la fecha. */
    public static String isoDate(LocalDateTime value) {
        return value == null ? null : value.toLocalDate().toString();
    }

    public static LocalDateTime startOfDay(LocalDate value) {
        return value == null ? null : value.atStartOfDay();
    }
}
