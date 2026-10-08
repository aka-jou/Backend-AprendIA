package com.aprendia.backend.feature.user.dto;

import lombok.Builder;

/** Elemento del listado GET /api/v1/users (forma exacta de la sección 4.3 de la spec). */
@Builder
public record UserSummaryDTO(
    Long id,
    String username,
    String nombreCompleto,
    String email,
    String role,
    String adscripcion,
    String status,
    String curp,
    String createdAt
) {}
