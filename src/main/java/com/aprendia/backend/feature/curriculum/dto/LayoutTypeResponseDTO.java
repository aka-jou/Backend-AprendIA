package com.aprendia.backend.feature.curriculum.dto;

import lombok.Builder;

@Builder
public record LayoutTypeResponseDTO(
    Long idTipoLayout,
    String nombre,
    String descripcion,
    String createdAt,
    String updatedAt
) {}
