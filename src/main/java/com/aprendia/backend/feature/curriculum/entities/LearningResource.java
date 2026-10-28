package com.aprendia.backend.feature.curriculum.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Recurso didáctico de un tema (anexo curricular, sección 5). Se llama LearningResource para no
 * confundirse con {@code org.springframework.core.io.Resource}; la tabla es {@code resources}.
 */
@Entity
@Table(name = "resources")
@Getter
@Setter
@NoArgsConstructor
public class LearningResource extends CurriculumAuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    /** "idLayout" de la spec: plantilla visual que interpreta el frontend. Sin catálogo en el backend. */
    @Column(name = "layout_id")
    private Integer layoutId;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    /** Columna JSONB; Hibernate la (de)serializa con Jackson. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private ResourceContent content;
}
