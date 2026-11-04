package com.aprendia.backend.feature.curriculum.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Fila de reactivo_previous_skills (reactivo_habilidades_prev del diagrama). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReactivoPreviousSkill {

    @Column(name = "skill_id", nullable = false)
    private Long skillId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
