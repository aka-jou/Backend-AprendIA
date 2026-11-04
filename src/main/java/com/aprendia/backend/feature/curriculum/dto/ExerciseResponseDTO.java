package com.aprendia.backend.feature.curriculum.dto;

import lombok.Builder;

import java.util.Map;

/** Respuesta de /exercises: cada campo va con su nombre web y su alias móvil. */
@Builder
public record ExerciseResponseDTO(
    String id,
    String idEjercicio,
    String reactivoId,
    String idReactivo,
    Integer order,
    Integer orden,
    String templateType,
    String instruction,
    String instruccion,
    Map<String, Object> config,
    Map<String, Object> contexto
) {}
