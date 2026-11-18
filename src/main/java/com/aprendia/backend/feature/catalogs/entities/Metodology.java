package com.aprendia.backend.feature.catalogs.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "metodologies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Metodology {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    /** "sigla" en la API. Columna de V5. */
    @Column(length = 10)
    private String acronym;

    @Column(length = 150)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean status = Boolean.TRUE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 50, nullable = false, updatable = false)
    private String createdBy;
}
