package com.aprendia.backend.feature.user.entities;

import com.aprendia.backend.security.crypto.EncryptedStringConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Familiar o tutor vinculado a un estudiante.
 *
 * {@code name} guarda el nombre completo calculado a partir de las partes (firstName, middleName,
 * lastName, secondLastName). Los familiares registrados antes de V22 solo tienen name, relationship
 * y phone; el resto de columnas queda en null para ellos.
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

    @Column(length = 150, nullable = false)
    private String name;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "second_last_name", length = 100)
    private String secondLastName;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 255)
    private String curp;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    // La columna es CHAR(1) (bpchar): sin SqlTypes.CHAR, ddl-auto=validate espera VARCHAR y falla.
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 1, columnDefinition = "CHAR(1)")
    private String gender;

    @Column(length = 50, nullable = false)
    private String relationship;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 255)
    private String phone;

    @Column(length = 100)
    private String email;

    @Embedded
    private RelativeAddress address;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
