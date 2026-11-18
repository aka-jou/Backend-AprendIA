package com.aprendia.backend.feature.curriculum.dto;

import lombok.Builder;

@Builder
public record UnitResponseDTO(
    Long idUnidad,
    Long idCuadernillo,
    String nombre,
    String urlImagen,
    Integer orden,
    Long totalTemas,
    Boolean status,
    String createdAt,
    String updatedAt
) {}
