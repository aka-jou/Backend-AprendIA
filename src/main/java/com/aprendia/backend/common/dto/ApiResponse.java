package com.aprendia.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Envelope estándar de TODAS las respuestas de la API (Especificación Técnica v2.0, sección 2.1).
 *
 * Éxito: { "success": true,  "data": {...} | [...], "message": "Operación completada con éxito." }
 * Error: { "success": false, "data": null, "message": "Descripción clara del error.",
 *          "errors": [ { "status": "409", "detail": "Descripción clara del error." } ] }
 *
 * errors solo aparece en las respuestas de error: repite el mensaje en el formato errors[].detail que
 * lee el frontend web. En las respuestas de éxito se omite.
 *
 * Los controllers NO necesitan construirlo: {@code ApiResponseAdvice} envuelve automáticamente
 * lo que regresen. Solo úsalo directamente si necesitas un mensaje calculado en tiempo de ejecución.
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<ApiError> errors
) {
    public static final String DEFAULT_SUCCESS_MESSAGE = "Operación completada con éxito.";

    /** Elemento de errors[]: status es el código HTTP como texto ("409"). */
    public record ApiError(String status, String detail) {
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ok(data, DEFAULT_SUCCESS_MESSAGE);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, null);
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(false, null, message, List.of(new ApiError(String.valueOf(status), message)));
    }
}
