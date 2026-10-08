package com.aprendia.backend.common.response;

import com.aprendia.backend.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Escribe el envelope de error directamente sobre el {@link HttpServletResponse}.
 *
 * Se usa en los puntos que corren ANTES de llegar a un controller (filtros y handlers de
 * Spring Security), donde {@code GlobalExceptionHandler} no puede intervenir.
 */
@Component
public class ApiResponseWriter {

    private final ObjectMapper objectMapper;

    public ApiResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(message));
    }
}
