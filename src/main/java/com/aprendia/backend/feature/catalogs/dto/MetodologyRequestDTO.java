package com.aprendia.backend.feature.catalogs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record MetodologyRequestDTO(

    @NotBlank(message = "El nombre de la metodología es obligatorio.")
    String nombre,

    @NotBlank(message = "La sigla es obligatoria.")
    @Size(max = 10, message = "La sigla no puede superar los 10 caracteres.")
    String sigla
) {}
