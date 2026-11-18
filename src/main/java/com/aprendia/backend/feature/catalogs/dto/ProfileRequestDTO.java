package com.aprendia.backend.feature.catalogs.dto;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ProfileRequestDTO(
    @NotBlank(message = "El nombre del perfil es obligatorio.")
    @Size(max = 100, message = "El nombre del perfil no puede superar los 100 caracteres.")
    String nombre,

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
    String descripcion,

    /** Opcional (alias "activo"): en POST vale true y en PUT, si se omite, se conserva. */
    @JsonAlias("activo")
    Boolean status
) {}
