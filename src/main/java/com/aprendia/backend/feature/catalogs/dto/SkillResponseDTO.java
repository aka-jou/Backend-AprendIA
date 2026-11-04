package com.aprendia.backend.feature.catalogs.dto;

import lombok.Builder;

/** SkillCatalogDto de la spec: id e idHabilidad llevan el mismo valor. */
@Builder
public record SkillResponseDTO(
    Long id,
    Long idHabilidad,
    String nombre,
    String descripcion,
    Long idCategoria,
    String nombreCategoria,
    Boolean status
) {}
