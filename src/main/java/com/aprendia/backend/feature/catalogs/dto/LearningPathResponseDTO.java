package com.aprendia.backend.feature.catalogs.dto;

import lombok.Builder;

@Builder
public record LearningPathResponseDTO(
    Long id,
    String nombre,
    Long idMetodologia,
    String nombreMetodologia,
    Boolean status
) {}
