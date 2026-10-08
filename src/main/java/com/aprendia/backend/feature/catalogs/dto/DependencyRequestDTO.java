package com.aprendia.backend.feature.catalogs.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record DependencyRequestDTO(

    @NotBlank(message = "El nombre de la dependencia es obligatorio.")
    String nombre,

    @NotBlank(message = "El código interno es obligatorio.")
    String codigoInterno
) {}
