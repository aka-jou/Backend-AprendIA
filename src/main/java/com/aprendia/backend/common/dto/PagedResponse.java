package com.aprendia.backend.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Estructura paginada que viaja dentro de {@code data} en los listados,
 * según la Especificación Técnica v2.0 (sección 2.2):
 * { "content": [...], "totalElements": 25, "totalPages": 3, "size": 10, "number": 0 }
 */
public record PagedResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int size,
        int number
) {
    public static <T> PagedResponse<T> fromPage(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber()
        );
    }
}
