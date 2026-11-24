package com.aprendia.backend.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Log de acceso: una entrada por petición HTTP en logs/access/access-AAAA-MM-DD.log.
 *
 * Corre antes que Spring Security (orden HIGHEST_PRECEDENCE + 10, después del ForwardedHeaderFilter),
 * por lo que también registra las peticiones rechazadas por API key o JWT (401/403).
 *
 * Además coloca en el MDC el identificador de petición y la IP del cliente: todos los logs emitidos
 * durante la petición (aplicación, auditoría, errores) llevan esos campos y se pueden correlacionar.
 * El identificador se devuelve al cliente en la cabecera X-Request-Id.
 *
 * Nivel del evento según el código de respuesta: 2xx/3xx INFO, 4xx WARN, 5xx ERROR.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AccessLogFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REAL_IP_HEADER = "X-Real-IP";
    /** Solo se reutiliza un X-Request-Id entrante si tiene un formato seguro (evita inyección en logs). */
    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("[A-Za-z0-9-]{8,64}");

    private static final Logger ACCESS = LoggerFactory.getLogger("ACCESS");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        long start = System.nanoTime();
        String requestId = resolveRequestId(request);
        MDC.put(LogFields.REQUEST_ID, requestId);
        MDC.put(LogFields.CLIENT_IP, resolveClientIp(request));
        response.setHeader(REQUEST_ID_HEADER, requestId);

        boolean failed = false;
        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException ex) {
            failed = true;
            throw ex;
        } finally {
            try {
                writeAccessLog(request, response, start, failed);
            } finally {
                MDC.remove(LogFields.REQUEST_ID);
                MDC.remove(LogFields.CLIENT_IP);
                MDC.remove(LogFields.USER_NAME);
            }
        }
    }

    private void writeAccessLog(HttpServletRequest request, HttpServletResponse response, long start, boolean failed) {
        try {
            int status = response.getStatus();
            if (failed && status < HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
            long durationNanos = System.nanoTime() - start;
            long durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNanos);

            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put(LogFields.EVENT_DATASET, LogFields.DATASET_ACCESS);
            fields.put(LogFields.HTTP_METHOD, request.getMethod());
            fields.put(LogFields.URL_PATH, request.getRequestURI());
            fields.put(LogFields.URL_QUERY, LogFields.truncate(request.getQueryString(), 500));
            fields.put(LogFields.HTTP_STATUS, status);
            fields.put(LogFields.EVENT_DURATION, durationNanos);
            fields.put(LogFields.EVENT_OUTCOME, status < 400 ? "success" : "failure");
            fields.put(LogFields.USER_AGENT, LogFields.truncate(request.getHeader("User-Agent"), 300));
            if (MDC.get(LogFields.USER_NAME) == null) {
                fields.put(LogFields.USER_NAME, "anonymous");
            }

            Level level = status >= 500 ? Level.ERROR : status >= 400 ? Level.WARN : Level.INFO;
            String path = request.getQueryString() == null
                    ? request.getRequestURI()
                    : request.getRequestURI() + "?" + LogFields.truncate(request.getQueryString(), 500);
            String message = request.getMethod() + " " + path + " " + status + " " + durationMillis + " ms";

            LogFields.log(ACCESS, level, fields, message);
        } catch (RuntimeException ex) {
            // Un fallo al escribir el log de acceso nunca debe afectar la respuesta al cliente.
            logger.warn("No se pudo escribir el log de acceso: " + ex.getMessage());
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String incoming = request.getHeader(REQUEST_ID_HEADER);
        if (incoming != null && SAFE_REQUEST_ID.matcher(incoming).matches()) {
            return incoming;
        }
        return UUID.randomUUID().toString();
    }

    /**
     * IP real del cliente. En la VPS la API solo escucha en 127.0.0.1 y Nginx envía X-Real-IP con la IP
     * restaurada desde Cloudflare (CF-Connecting-IP), por lo que esa cabecera es confiable. Fuera de ese
     * esquema se usa la dirección remota, ya resuelta por el ForwardedHeaderFilter
     * (server.forward-headers-strategy=framework).
     */
    private String resolveClientIp(HttpServletRequest request) {
        String realIp = request.getHeader(REAL_IP_HEADER);
        if (StringUtils.hasText(realIp) && realIp.length() <= 45) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
