package com.aprendia.backend.feature.user.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Vínculo entre un estudiante y un familiar o tutor (tabla intermedia student_relatives, V28).
 * Los datos del familiar están en {@link Person}: un tutor con varios hijos inscritos es una sola
 * persona con un vínculo por cada hijo. Un mismo familiar no se vincula dos veces al mismo alumno.
 */
@Entity
@Table(name = "student_relatives")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRelative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    /** Parentesco con el alumno (Madre, Padre, Tutor...). */
    @Column(length = 50, nullable = false)
    private String relationship;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
