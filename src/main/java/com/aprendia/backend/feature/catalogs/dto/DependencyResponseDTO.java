package com.aprendia.backend.feature.catalogs.dto;

import lombok.Builder;

@Builder
public record DependencyResponseDTO(
    Long id,
    String nombre,
    String tipo,
    String codigoInterno,
    Boolean status,
    String createdAt,
    String createdBy
) {}
