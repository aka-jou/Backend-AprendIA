package com.aprendia.backend.common.dto;

/**
 * Envelope estándar de TODAS las respuestas de la API (Especificación Técnica v2.0, sección 2.1).
 *
 * Éxito: { "success": true,  "data": {...} | [...], "message": "Operación completada con éxito." }
 * Error: { "success": false, "data": null,          "message": "Descripción clara del error." }
 *
 * Los controllers NO necesitan construirlo: {@code ApiResponseAdvice} envuelve automáticamente
 * lo que regresen. Solo úsalo directamente si necesitas un mensaje calculado en tiempo de ejecución.
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        String message
) {
    public static final String DEFAULT_SUCCESS_MESSAGE = "Operación completada con éxito.";

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ok(data, DEFAULT_SUCCESS_MESSAGE);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
