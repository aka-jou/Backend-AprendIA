package com.aprendia.backend.feature.curriculum.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import jakarta.validation.constraints.*;
import lombok.Builder;

/** Cuerpo de POST y PUT /topics (anexo curricular, sección 4.2). */
@Builder
public record TopicRequestDTO(
    @NotNull(message = "La unidad (idUnidad) es obligatoria.")
    @Positive(message = "idUnidad debe ser un número positivo.")
    Long idUnidad,

    @NotBlank(message = "El nombre del tema es obligatorio.")
    @Size(max = 150, message = "El nombre del tema no puede superar los 150 caracteres.")
    String nombre,

    @Size(max = 5000, message = "La descripción no puede superar los 5000 caracteres.")
    String descripcion,

    @Size(max = 1000, message = "urlImagen no puede superar los 1000 caracteres.")
    @Pattern(regexp = ValidationPatterns.HTTP_URL, message = "urlImagen debe ser una URL http(s) válida.")
    String urlImagen,

    @Min(value = 1, message = "El orden debe ser mayor o igual a 1.")
    Integer orden
) {}
