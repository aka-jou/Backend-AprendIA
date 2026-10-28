package com.aprendia.backend.feature.curriculum.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

/** Cuerpo de POST y PUT /resources (anexo curricular, sección 5.1). */
@Builder
public record ResourceRequestDTO(
    @NotNull(message = "El tema (idTema) es obligatorio.")
    @Positive(message = "idTema debe ser un número positivo.")
    Long idTema,

    /** Opcional: la spec no define el catálogo de layouts; se guarda el entero que envía el frontend. */
    @Min(value = 1, message = "idLayout debe ser mayor o igual a 1.")
    Integer idLayout,

    @NotBlank(message = "El título del recurso es obligatorio.")
    @Size(max = 200, message = "El título del recurso no puede superar los 200 caracteres.")
    String titulo,

    @Size(max = 1000, message = "urlImagen no puede superar los 1000 caracteres.")
    @Pattern(regexp = ValidationPatterns.HTTP_URL, message = "urlImagen debe ser una URL http(s) válida.")
    String urlImagen,

    @NotNull(message = "El contenido del recurso es obligatorio.")
    @Valid
    ResourceContentDTO contenido
) {}
