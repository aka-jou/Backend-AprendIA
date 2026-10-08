package com.aprendia.backend.feature.catalogs.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ProfileRequestDTO(

    @NotBlank(message = "El nombre del perfil es obligatorio.")
    String nombre,

    @NotNull(message = "El nivel de acceso es obligatorio.")
    @Min(value = 1, message = "El nivel de acceso debe ser mayor o igual a 1.")
    Integer nivelAcceso
) {}
