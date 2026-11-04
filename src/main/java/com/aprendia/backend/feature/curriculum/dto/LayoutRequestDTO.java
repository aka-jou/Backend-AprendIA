package com.aprendia.backend.feature.curriculum.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Map;

/** Cuerpo de POST y PUT /layouts. */
public record LayoutRequestDTO(
    @NotNull(message = "El tipo de layout (idTipoLayout) es obligatorio.")
    @Positive(message = "idTipoLayout debe ser un número positivo.")
    Long idTipoLayout,

    @NotBlank(message = "El código del layout es obligatorio.")
    @Pattern(regexp = "^[a-z0-9_]{2,50}$",
             message = "El código del layout solo admite minúsculas, dígitos y guion bajo (2 a 50 caracteres), ej. identify_family.")
    String codigo,

    @NotBlank(message = "El nombre del layout es obligatorio.")
    @Size(max = 150, message = "El nombre del layout no puede superar los 150 caracteres.")
    String nombre,

    @NotBlank(message = "La descripción del layout es obligatoria.")
    @Size(max = 2000, message = "La descripción del layout no puede superar los 2000 caracteres.")
    String descripcion,

    @Size(max = 1000, message = "urlImagen no puede superar los 1000 caracteres.")
    @Pattern(regexp = ValidationPatterns.HTTP_URL, message = "urlImagen debe ser una URL http(s) válida.")
    String urlImagen,

    @NotNull(message = "Los atributos del layout son obligatorios (objeto JSON, puede ser {}).")
    Map<String, Object> atributos,

    @NotNull(message = "isActive es obligatorio (true o false).")
    Boolean isActive
) {}
