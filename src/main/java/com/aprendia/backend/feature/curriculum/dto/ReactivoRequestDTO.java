package com.aprendia.backend.feature.curriculum.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Cuerpo de POST y PUT /reactivos (ReactivoModalComponent).
 *
 * topicId, topicName, layoutCode, layoutName y targetSkills se aceptan pero se ignoran: el servidor
 * los calcula a partir de idTema, idLayout y habilidades para que nunca se contradigan.
 */
public record ReactivoRequestDTO(
    @NotNull(message = "El tema (idTema) es obligatorio.")
    @Positive(message = "idTema debe ser un número positivo.")
    Long idTema,

    String topicId,
    String topicName,

    @NotNull(message = "El layout (idLayout) es obligatorio.")
    @Positive(message = "idLayout debe ser un número positivo.")
    Long idLayout,

    String layoutCode,
    String layoutName,

    @NotBlank(message = "El título del reactivo es obligatorio.")
    @Size(max = 200, message = "El título del reactivo no puede superar los 200 caracteres.")
    String title,

    @NotBlank(message = "Las instrucciones del reactivo son obligatorias.")
    @Size(max = 5000, message = "Las instrucciones del reactivo no pueden superar los 5000 caracteres.")
    String instructions,

    @NotBlank(message = "La imagen de portada (coverImageUrl) es obligatoria.")
    @Size(max = 1000, message = "coverImageUrl no puede superar los 1000 caracteres.")
    @Pattern(regexp = ValidationPatterns.HTTP_URL, message = "coverImageUrl debe ser una URL http(s) válida.")
    String coverImageUrl,

    @NotNull(message = "El tiempo sugerido (suggestedTimeMinutes) es obligatorio.")
    @Min(value = 1, message = "suggestedTimeMinutes debe ser mayor o igual a 1.")
    Integer suggestedTimeMinutes,

    @NotBlank(message = "El estado del reactivo es obligatorio.")
    @Pattern(regexp = "^(active|draft|inactive)$", message = "status debe ser 'active', 'draft' o 'inactive'.")
    String status,

    @NotEmpty(message = "El reactivo debe tener al menos una habilidad (habilidades).")
    @Valid
    List<ReactivoSkillRequestDTO> habilidades,

    List<String> targetSkills,

    List<String> previousSkills,

    @NotEmpty(message = "El reactivo debe tener al menos un ejercicio (exercises).")
    @Valid
    List<ExerciseRequestDTO> exercises
) {}
