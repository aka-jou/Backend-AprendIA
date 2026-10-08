package com.aprendia.backend.feature.student.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/** Elemento de "relatives": tutor o familiar del alumno (entrada y salida). */
@Builder
public record StudentRelativeDTO(
    @NotBlank(message = "El nombre completo del familiar es obligatorio.")
    @Size(max = 150, message = "El nombre del familiar no puede superar los 150 caracteres.")
    String name,

    @NotBlank(message = "El parentesco es obligatorio (ej. Madre, Padre, Tutor Legal, Abuelo/a).")
    @Size(max = 50, message = "El parentesco no puede superar los 50 caracteres.")
    String relationship,

    @Pattern(regexp = ValidationPatterns.PHONE_10, message = "El teléfono del familiar debe tener exactamente 10 dígitos.")
    String phone
) {}
