package com.aprendia.backend.feature.catalogs.dto;

import lombok.Builder;

@Builder
public record DependencyResponseDTO(
    Long id,
    String nombre,
    String codigoInterno,
    String createdAt,
    String createdBy
) {}
