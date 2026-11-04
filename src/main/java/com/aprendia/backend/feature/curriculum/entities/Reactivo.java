package com.aprendia.backend.feature.curriculum.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

/**
 * Reactivo pedagógico de un tema: datos generales (portada, instrucciones, tiempo sugerido,
 * habilidades) y, aparte, su lista de ejercicios ({@link Exercise}).
 *
 * Las dos listas de habilidades se reemplazan completas en cada guardado (clear + add): Hibernate
 * borra las filas anteriores antes de insertar las nuevas, así la llave primaria nunca choca.
 */
@Entity
@Table(name = "reactivos")
@Getter
@Setter
@NoArgsConstructor
public class Reactivo extends CurriculumAuditFields {

    public static final String STATUS_ACTIVE = "active";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "layout_id", nullable = false)
    private Layout layout;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String instructions;

    /** coverImageUrl de la spec. */
    @Column(name = "image_url", length = 1000, nullable = false)
    private String imageUrl;

    @Column(name = "suggested_time_minutes", nullable = false)
    private Integer suggestedTimeMinutes;

    /** active | draft | inactive. */
    @Column(length = 10, nullable = false)
    private String status;

    @ElementCollection
    @CollectionTable(name = "reactivo_skills", joinColumns = @JoinColumn(name = "reactivo_id"))
    @OrderBy("sortOrder ASC")
    @BatchSize(size = 50)
    private List<ReactivoSkill> skills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "reactivo_previous_skills", joinColumns = @JoinColumn(name = "reactivo_id"))
    @OrderBy("sortOrder ASC")
    @BatchSize(size = 50)
    private List<ReactivoPreviousSkill> previousSkills = new ArrayList<>();
}
