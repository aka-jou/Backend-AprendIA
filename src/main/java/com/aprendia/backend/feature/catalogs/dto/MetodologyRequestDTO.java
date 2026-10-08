package com.aprendia.backend.feature.catalogs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record MetodologyRequestDTO(
    @NotBlank(message = "El nombre de la metodología es obligatorio.")
    @Size(max = 100, message = "El nombre de la metodología no puede superar los 100 caracteres.")
    String nombre,

    @Size(max = 150, message = "El autor no puede superar los 150 caracteres.")
    String autor,

    @NotNull(message = "El estatus es obligatorio.")
    Boolean status
) {}
