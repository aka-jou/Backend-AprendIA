package com.aprendia.backend.feature.curriculum.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record LayoutResponseDTO(
    Long idLayout,
    Long idTipoLayout,
    String codigo,
    String nombre,
    String descripcion,
    String urlImagen,
    Map<String, Object> atributos,
    Boolean isActive,
    String createdAt,
    String updatedAt
) {}
