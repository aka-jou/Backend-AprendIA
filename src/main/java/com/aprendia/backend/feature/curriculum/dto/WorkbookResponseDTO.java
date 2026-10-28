package com.aprendia.backend.feature.curriculum.dto;

import lombok.Builder;

/**
 * Cuadernillo. Todas las operaciones devuelven el mismo conjunto de campos: es la unión de los
 * ejemplos de la spec (listado: nombreRuta y totalUnidades; detalle: createdAt y updatedAt).
 */
@Builder
public record WorkbookResponseDTO(
    Long idCuadernillo,
    Long idRutaAprendizaje,
    String nombreRuta,
    String nombre,
    String descripcion,
    String objetivo,
    String autor,
    String urlImagen,
    Boolean status,
    Integer orden,
    Long totalUnidades,
    String createdAt,
    String updatedAt
) {}
