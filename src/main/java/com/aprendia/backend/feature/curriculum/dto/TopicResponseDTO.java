package com.aprendia.backend.feature.curriculum.dto;

import lombok.Builder;

/**
 * Tema curricular. totalReactivos se devuelve siempre en 0: el módulo de reactivos no está
 * especificado todavía; el campo se mantiene para no romper el contrato con el frontend.
 */
@Builder
public record TopicResponseDTO(
    Long idTema,
    Long idUnidad,
    String nombre,
    String descripcion,
    String urlImagen,
    Integer orden,
    Long totalRecursos,
    Long totalReactivos,
    String createdAt,
    String updatedAt
) {}
