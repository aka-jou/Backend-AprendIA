package com.aprendia.backend.feature.curriculum.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Elemento de habilidades[] en POST y PUT /reactivos. nombreHabilidad se ignora: sale del catálogo. */
public record ReactivoSkillRequestDTO(
    @NotNull(message = "Cada habilidad debe indicar idHabilidad.")
    @Positive(message = "idHabilidad debe ser un número positivo.")
    Long idHabilidad,

    String nombreHabilidad,

    @NotNull(message = "Cada habilidad debe indicar su peso.")
    @DecimalMin(value = "0", inclusive = false, message = "El peso de cada habilidad debe ser mayor que 0.")
    @DecimalMax(value = "100", message = "El peso de cada habilidad no puede superar 100.")
    @Digits(integer = 3, fraction = 2, message = "El peso admite como máximo 2 decimales.")
    BigDecimal peso
) {}
