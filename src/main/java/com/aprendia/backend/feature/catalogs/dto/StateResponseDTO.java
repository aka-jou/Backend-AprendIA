package com.aprendia.backend.feature.catalogs.dto;

import lombok.Builder;

@Builder
public record StateResponseDTO(
    Long id,
    String nombre,
    Boolean activo,
    String createdAt,
    String createdBy
) {}
