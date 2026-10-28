package com.aprendia.backend.feature.curriculum.dto;

import lombok.Builder;

/** Recurso didáctico. Además de los campos del ejemplo de la spec, incluye el contenido completo. */
@Builder
public record ResourceResponseDTO(
    Long idRecurso,
    Long idTema,
    Integer idLayout,
    String titulo,
    String urlImagen,
    ResourceContentDTO contenido,
    String createdAt,
    String updatedAt
) {}
