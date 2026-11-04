package com.aprendia.backend.feature.catalogs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST y PUT /catalogs/skills. status es opcional (por defecto true). */
public record SkillRequestDTO(
    @NotBlank(message = "El nombre de la habilidad es obligatorio.")
    @Size(max = 100, message = "El nombre de la habilidad no puede superar los 100 caracteres.")
    String nombre,

    @Size(max = 500, message = "La descripción de la habilidad no puede superar los 500 caracteres.")
    String descripcion,

    @Positive(message = "idCategoria debe ser un número positivo.")
    Long idCategoria,

    Boolean status
) {}
