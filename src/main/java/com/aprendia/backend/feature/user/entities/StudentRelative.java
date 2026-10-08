package com.aprendia.backend.feature.user.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Familiar o tutor vinculado a un estudiante: { name, relationship, phone }. */
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

    @Column(length = 150, nullable = false)
    private String name;

    @Column(length = 50, nullable = false)
    private String relationship;

    @Convert(converter = com.aprendia.backend.security.crypto.EncryptedStringConverter.class)
    @Column(length = 255)
    private String phone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
