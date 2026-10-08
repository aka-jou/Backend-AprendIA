package com.aprendia.backend.feature.catalogs.dto;

import lombok.Builder;

@Builder
public record ProfileResponseDTO(
    Long id,
    String nombre,
    Integer nivelAcceso
) {}
