package com.aprendia.backend.security.filter;

import com.aprendia.backend.common.response.ApiResponseWriter;
import com.aprendia.backend.security.crypto.ApiKeyHasher;
import com.aprendia.backend.security.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Primera capa de seguridad de la Especificación v2.0: cabecera X-API-KEY obligatoria.
 * Se registra dentro de la cadena de Spring Security (SecurityConfig), antes del filtro JWT.
 *
 * Exentos: preflight CORS (OPTIONS), documentación Swagger/OpenAPI y health checks de monitoreo.
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-KEY";

    /** Rutas relativas al context-path (/api) que no requieren API key. */
    private static final List<String> EXEMPT_PREFIXES = List.of(
            "/swagger-ui", "/v3/api-docs", "/webjars", "/actuator/health", "/v1/health", "/error");

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private ApiResponseWriter apiResponseWriter;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.equals("/swagger-ui.html") || EXEMPT_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestApiKey = request.getHeader(API_KEY_HEADER);

        if (requestApiKey == null || requestApiKey.isBlank()) {
            apiResponseWriter.writeError(response, HttpStatus.UNAUTHORIZED, "Falta la cabecera X-API-KEY.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isValid = apiKeyRepository.findByKeyHash(ApiKeyHasher.sha256Hex(requestApiKey.trim()))
                .map(key -> !key.isDeprecated() && (key.getExpiresAt() == null || key.getExpiresAt().isAfter(now)))
                .orElse(false);

        if (!isValid) {
            apiResponseWriter.writeError(response, HttpStatus.UNAUTHORIZED, "La API Key es inválida, está obsoleta o expiró.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
