package com.aprendia.backend.feature.curriculum.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Ejercicio en POST /reactivos/{id}/exercises, PUT /exercises/{id} y dentro de exercises[] de
 * POST y PUT /reactivos.
 *
 * id solo se usa en PUT /reactivos: si viene, actualiza ese ejercicio en lugar de crear uno nuevo.
 * templateType es opcional; si viene, debe ser el código del layout del reactivo.
 */
public record ExerciseRequestDTO(
    String id,

    @NotNull(message = "Cada ejercicio debe indicar su orden (order).")
    @Min(value = 1, message = "El orden del ejercicio debe ser mayor o igual a 1.")
    Integer order,

    String templateType,

    @NotBlank(message = "Cada ejercicio debe tener instrucción (instruction).")
    @Size(max = 2000, message = "La instrucción del ejercicio no puede superar los 2000 caracteres.")
    String instruction,

    @NotNull(message = "Cada ejercicio debe tener config (objeto JSON con los datos de la plantilla).")
    Map<String, Object> config
) {}
