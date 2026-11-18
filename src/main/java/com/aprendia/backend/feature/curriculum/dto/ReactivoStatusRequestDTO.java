package com.aprendia.backend.feature.curriculum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Cuerpo de PATCH /reactivos/{id}/status: cambia solo el estado, sin reenviar habilidades ni ejercicios. */
public record ReactivoStatusRequestDTO(
    @NotBlank(message = "El estado del reactivo es obligatorio.")
    @Pattern(regexp = "^(active|draft|inactive)$", message = "status debe ser 'active', 'draft' o 'inactive'.")
    String status
) {}
