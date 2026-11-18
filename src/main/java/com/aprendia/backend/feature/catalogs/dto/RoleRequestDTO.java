package com.aprendia.backend.feature.catalogs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;

@Builder
public record RoleRequestDTO(

    @NotBlank(message = "El nombre del rol es obligatorio.")
    @Size(max = 50, message = "El nombre del rol no puede superar los 50 caracteres.")
    String nombre,

    String descripcion,

    /** Opcional (alias "activo"). Si se omite en PUT se conserva el actual; en POST vale true. */
    @JsonAlias("activo")
    Boolean status
) {}
