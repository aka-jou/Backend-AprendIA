package com.aprendia.backend.feature.curriculum.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import jakarta.validation.constraints.*;
import lombok.Builder;

/** Cuerpo de POST y PUT /workbooks (anexo curricular, secciones 2.1, 2.4 y 2.5). */
@Builder
public record WorkbookRequestDTO(
    @NotNull(message = "La ruta de aprendizaje (idRutaAprendizaje) es obligatoria.")
    @Positive(message = "idRutaAprendizaje debe ser un número positivo.")
    Long idRutaAprendizaje,

    @NotBlank(message = "El nombre del cuadernillo es obligatorio.")
    @Size(max = 150, message = "El nombre del cuadernillo no puede superar los 150 caracteres.")
    String nombre,

    @NotBlank(message = "La descripción del cuadernillo es obligatoria.")
    @Size(max = 5000, message = "La descripción no puede superar los 5000 caracteres.")
    String descripcion,

    @NotBlank(message = "El objetivo del cuadernillo es obligatorio.")
    @Size(max = 5000, message = "El objetivo no puede superar los 5000 caracteres.")
    String objetivo,

    @NotBlank(message = "El autor del cuadernillo es obligatorio.")
    @Size(max = 200, message = "El autor no puede superar los 200 caracteres.")
    String autor,

    @NotBlank(message = "La imagen de portada (urlImagen) es obligatoria.")
    @Size(max = 1000, message = "urlImagen no puede superar los 1000 caracteres.")
    @Pattern(regexp = ValidationPatterns.HTTP_URL, message = "urlImagen debe ser una URL http(s) válida.")
    String urlImagen,

    @NotNull(message = "El estatus es obligatorio.")
    Boolean status,

    /** Opcional. En POST el valor por defecto es 1; en PUT, si se omite, se conserva el actual. */
    @Min(value = 1, message = "El orden debe ser mayor o igual a 1.")
    Integer orden
) {}
