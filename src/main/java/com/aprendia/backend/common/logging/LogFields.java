package com.aprendia.backend.common.logging;

import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Nombres de campo (Elastic Common Schema 8.11) y utilidades para escribir logs estructurados.
 *
 * Los campos se entregan al encoder ECS de Spring Boot a través del MDC: el encoder vuelca todas las
 * entradas del MDC como claves de primer nivel del JSON. {@link #log} coloca los campos solo durante la
 * llamada al logger y restaura después los valores previos, de modo que no se filtran a otros eventos.
 */
public final class LogFields {

    // ---------- Contexto de la petición (presentes en todos los logs emitidos durante la petición) ----------
    /** Identificador único de la petición; correlaciona acceso, auditoría y errores. */
    public static final String REQUEST_ID = "http.request.id";
    /** IP real del cliente (X-Real-IP de Nginx; en su defecto, la dirección remota). */
    public static final String CLIENT_IP = "client.ip";
    /** Usuario autenticado (JWT) o actor de la acción auditada. */
    public static final String USER_NAME = "user.name";

    // ---------- Evento ----------
    /** Tipo de log: aprendia.access | aprendia.audit. */
    public static final String EVENT_DATASET = "event.dataset";
    public static final String EVENT_ACTION = "event.action";
    public static final String EVENT_CATEGORY = "event.category";
    public static final String EVENT_TYPE = "event.type";
    /** success | failure. */
    public static final String EVENT_OUTCOME = "event.outcome";
    public static final String EVENT_REASON = "event.reason";
    /** Duración en nanosegundos (convención ECS). */
    public static final String EVENT_DURATION = "event.duration";

    // ---------- HTTP ----------
    public static final String HTTP_METHOD = "http.request.method";
    public static final String HTTP_STATUS = "http.response.status_code";
    public static final String URL_PATH = "url.path";
    public static final String URL_QUERY = "url.query";
    public static final String USER_AGENT = "user_agent.original";

    // ---------- Campos propios (espacio de nombres "aprendia.", permitido por ECS para campos personalizados) ----------
    public static final String RESOURCE_TYPE = "aprendia.resource.type";
    public static final String RESOURCE_ID = "aprendia.resource.id";
    public static final String PATH_PARAMS = "aprendia.path.params";
    public static final String NEW_STATUS = "aprendia.status.new";

    public static final String DATASET_ACCESS = "aprendia.access";
    public static final String DATASET_AUDIT = "aprendia.audit";

    private LogFields() {
    }

    /**
     * Escribe un evento con campos estructurados adicionales. Los valores nulos se omiten.
     * El mensaje se registra tal cual (sin interpretar marcadores {}).
     */
    public static void log(Logger logger, Level level, Map<String, ?> fields, String message) {
        Map<String, String> previous = new HashMap<>();
        try {
            fields.forEach((key, value) -> {
                if (value != null) {
                    previous.put(key, MDC.get(key));
                    MDC.put(key, String.valueOf(value));
                }
            });
            switch (level) {
                case ERROR -> logger.error("{}", message);
                case WARN -> logger.warn("{}", message);
                case DEBUG -> logger.debug("{}", message);
                case TRACE -> logger.trace("{}", message);
                default -> logger.info("{}", message);
            }
        } finally {
            previous.forEach((key, oldValue) -> {
                if (oldValue == null) {
                    MDC.remove(key);
                } else {
                    MDC.put(key, oldValue);
                }
            });
        }
    }

    /** Recorta textos libres (mensajes de excepción, user-agent) para acotar el tamaño de cada línea. */
    public static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }
}
