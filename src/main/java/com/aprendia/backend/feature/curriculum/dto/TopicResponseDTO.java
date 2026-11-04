package com.aprendia.backend.feature.curriculum.dto;

import lombok.Builder;

/**
 * Tema curricular. totalReactivos cuenta los reactivos vigentes del tema; sin permisos de gestión
 * solo cuenta los de status = active (los mismos que devuelve GET /reactivos).
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
