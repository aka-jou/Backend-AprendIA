package com.aprendia.backend.feature.catalogs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record LearningPathRequestDTO(
    @NotBlank(message = "El nombre de la ruta de aprendizaje es obligatorio.")
    @Size(max = 150, message = "El nombre de la ruta de aprendizaje no puede superar los 150 caracteres.")
    String nombre,

    @NotNull(message = "La metodología (idMetodologia) es obligatoria.")
    @Positive(message = "idMetodologia debe ser un número positivo.")
    Long idMetodologia,

    @NotNull(message = "El estatus es obligatorio.")
    Boolean status
) {}
