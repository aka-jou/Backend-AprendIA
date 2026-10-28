package com.aprendia.backend.feature.curriculum.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Campo "contenido" de un recurso: { "slides": [ ... ] }. */
public record ResourceContentDTO(
    @NotEmpty(message = "El contenido debe incluir al menos una diapositiva (slides).")
    @Size(max = 200, message = "El contenido no puede tener más de 200 diapositivas.")
    List<@Valid ResourceSlideDTO> slides
) {}
