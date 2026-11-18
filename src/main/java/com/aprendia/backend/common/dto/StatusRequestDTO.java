package com.aprendia.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de los PATCH .../{id}/status: activar o desactivar sin borrar.
 *
 * Acepta el booleano con cualquiera de los nombres que usa el frontend: {"activo": true},
 * {"status": true} o {"isActive": true}.
 */
public record StatusRequestDTO(
    @JsonAlias({"status", "isActive", "active"})
    @NotNull(message = "El campo 'activo' es obligatorio (true o false).")
    Boolean activo
) {}
