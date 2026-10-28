package com.aprendia.backend.feature.catalogs.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record LearningPathRequestDTO(
    @NotBlank(message = "El nombre de la ruta de aprendizaje es obligatorio.")
    @Size(max = 150, message = "El nombre de la ruta de aprendizaje no puede superar los 150 caracteres.")
    String nombre,

    @NotNull(message = "La metodología (idMetodologia) es obligatoria.")
    @Positive(message = "idMetodologia debe ser un número positivo.")
    Long idMetodologia,

    @NotNull(message = "El estatus es obligatorio.")
    Boolean status,

    /**
     * Opcional (lo expone el árbol curricular como "perfil"). Si se omite en PUT se conserva el actual,
     * para que el frontend que solo envía {nombre, idMetodologia, status} no lo borre.
     */
    @Positive(message = "idPerfil debe ser un número positivo.")
    Long idPerfil,

    /** Opcional. Si se omite en PUT se conserva la actual; una cadena vacía la elimina. */
    @Size(max = 1000, message = "urlImagen no puede superar los 1000 caracteres.")
    @Pattern(regexp = ValidationPatterns.HTTP_URL, message = "urlImagen debe ser una URL http(s) válida.")
    String urlImagen
) {}
