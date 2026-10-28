package com.aprendia.backend.feature.curriculum.entities;

import com.aprendia.backend.feature.catalogs.entities.LearningPath;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Cuadernillo de trabajo: unidad temática de una ruta de aprendizaje (anexo curricular, sección 2). */
@Entity
@Table(name = "workbooks")
@Getter
@Setter
@NoArgsConstructor
public class Workbook extends CurriculumAuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_path_id", nullable = false)
    private LearningPath learningPath;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String objective;

    @Column(length = 200, nullable = false)
    private String author;

    @Column(name = "image_url", length = 1000, nullable = false)
    private String imageUrl;

    /** Estado editorial de la spec: true activo, false inactivo o borrador. No es la baja lógica. */
    @Column(nullable = false)
    private Boolean status = Boolean.TRUE;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 1;
}
