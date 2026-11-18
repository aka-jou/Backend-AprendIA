package com.aprendia.backend.feature.catalogs.dto;

import lombok.Builder;

@Builder
public record MetodologyResponseDTO(
    Long id,
    String nombre,
    String sigla,
    String autor,
    String descripcion,
    String urlImagen,
    Boolean status
) {}
