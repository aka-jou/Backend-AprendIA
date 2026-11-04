package com.aprendia.backend.feature.curriculum.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Fila de reactivo_skills (reactivo_habilidades del diagrama): habilidad evaluada y su peso. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReactivoSkill {

    @Column(name = "skill_id", nullable = false)
    private Long skillId;

    @Column(name = "weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
