package com.aprendia.backend.feature.curriculum.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Unidad didáctica de un cuadernillo (anexo curricular, sección 3). */
@Entity
@Table(name = "units")
@Getter
@Setter
@NoArgsConstructor
public class Unit extends CurriculumAuditFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workbook_id", nullable = false)
    private Workbook workbook;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 1;

    /** false: desactivado (V29). Se conserva y se puede reactivar; quien no gestiona contenido no lo ve. */
    @Column(nullable = false)
    private Boolean status = Boolean.TRUE;
}
