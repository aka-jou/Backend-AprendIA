package com.aprendia.backend.feature.curriculum.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import jakarta.validation.constraints.*;
import lombok.Builder;

/** Cuerpo de POST y PUT /units (anexo curricular, sección 3.2). */
@Builder
public record UnitRequestDTO(
    @NotNull(message = "El cuadernillo (idCuadernillo) es obligatorio.")
    @Positive(message = "idCuadernillo debe ser un número positivo.")
    Long idCuadernillo,

    @NotBlank(message = "El nombre de la unidad es obligatorio.")
    @Size(max = 150, message = "El nombre de la unidad no puede superar los 150 caracteres.")
    String nombre,

    /** Opcional: la spec no lo marca como obligatorio fuera de los cuadernillos. */
    @Size(max = 1000, message = "urlImagen no puede superar los 1000 caracteres.")
    @Pattern(regexp = ValidationPatterns.HTTP_URL, message = "urlImagen debe ser una URL http(s) válida.")
    String urlImagen,

    @Min(value = 1, message = "El orden debe ser mayor o igual a 1.")
    Integer orden
) {}
