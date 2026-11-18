package com.aprendia.backend.feature.catalogs.dto;

import lombok.Builder;

@Builder
public record RoleResponseDTO(
    Integer id,
    String nombre,
    String descripcion,
    Boolean status,
    String createdAt,
    String createdBy
) {}
