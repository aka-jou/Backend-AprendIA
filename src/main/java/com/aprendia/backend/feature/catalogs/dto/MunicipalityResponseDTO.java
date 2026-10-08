package com.aprendia.backend.feature.catalogs.dto;

import lombok.Builder;

@Builder
public record MunicipalityResponseDTO(
    Long id,
    String nombre,
    Long estadoId,
    String estadoNombre,
    Boolean activo,
    String createdAt,
    String createdBy
) {}
