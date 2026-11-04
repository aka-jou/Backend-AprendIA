package com.aprendia.backend.feature.curriculum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST y PUT /layout-types. */
public record LayoutTypeRequestDTO(
    @NotBlank(message = "El nombre del tipo de layout es obligatorio.")
    @Size(max = 100, message = "El nombre del tipo de layout no puede superar los 100 caracteres.")
    String nombre,

    @NotBlank(message = "La descripción del tipo de layout es obligatoria.")
    @Size(max = 2000, message = "La descripción del tipo de layout no puede superar los 2000 caracteres.")
    String descripcion
) {}
