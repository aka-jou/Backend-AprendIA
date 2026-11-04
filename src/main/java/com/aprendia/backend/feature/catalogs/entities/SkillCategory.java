package com.aprendia.backend.feature.catalogs.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Categoría pedagógica de una habilidad (categoria_habilidad del diagrama). */
@Entity
@Table(name = "skill_categories")
@Getter
@Setter
@NoArgsConstructor
public class SkillCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 500)
    private String description;
}
