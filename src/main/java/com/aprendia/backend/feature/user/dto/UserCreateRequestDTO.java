package com.aprendia.backend.feature.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/** POST /api/v1/users — asistente de 3 pasos: persona, credenciales y asignación. */
@Builder
public record UserCreateRequestDTO(
    @Valid @NotNull(message = "La sección 'persona' es obligatoria.")
    UserPersonaDTO persona,

    @Valid @NotNull(message = "La sección 'credentials' es obligatoria.")
    UserCredentialsDTO credentials,

    @Valid @NotNull(message = "La sección 'assignment' es obligatoria.")
    UserAssignmentDTO assignment
) {}
