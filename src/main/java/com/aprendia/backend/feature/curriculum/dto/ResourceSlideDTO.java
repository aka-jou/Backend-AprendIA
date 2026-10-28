package com.aprendia.backend.feature.curriculum.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import jakarta.validation.constraints.*;

/** Diapositiva dentro de contenido.slides (anexo curricular, sección 5.1). */
public record ResourceSlideDTO(
    @NotNull(message = "Cada diapositiva debe indicar su orden (order).")
    @Min(value = 1, message = "El orden de la diapositiva debe ser mayor o igual a 1.")
    Integer order,

    @NotBlank(message = "Cada diapositiva debe tener título (title).")
    @Size(max = 200, message = "El título de la diapositiva no puede superar los 200 caracteres.")
    String title,

    @Size(max = 5000, message = "La descripción de la diapositiva no puede superar los 5000 caracteres.")
    String description,

    @Size(max = 1000, message = "imageUrl no puede superar los 1000 caracteres.")
    @Pattern(regexp = ValidationPatterns.HTTP_URL, message = "imageUrl debe ser una URL http(s) válida.")
    String imageUrl
) {}
