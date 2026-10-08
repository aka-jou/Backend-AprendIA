package com.aprendia.backend.feature.catalogs.dto;

import lombok.Builder;

@Builder
public record MetodologyResponseDTO(
    Long id,
    String nombre,
    String autor,
    Boolean status
) {}
