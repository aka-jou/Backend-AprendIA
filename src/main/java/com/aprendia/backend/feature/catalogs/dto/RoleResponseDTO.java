package com.aprendia.backend.feature.catalogs.dto;

import lombok.Builder;

@Builder
public record RoleResponseDTO(
    Integer id,
    String nombre,
    String descripcion,
    String createdAt,
    String createdBy
) {}
