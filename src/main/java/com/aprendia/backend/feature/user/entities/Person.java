package com.aprendia.backend.feature.user.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Persona: estudiante, usuario del personal o familiar/tutor. Un familiar es una sola fila aunque esté
 * vinculado a varios estudiantes (hermanos); el vínculo y el parentesco están en {@link StudentRelative}.
 * {@code email} y {@code address} solo se usan para familiares: el domicilio del estudiante está en
 * {@link Address} y el correo de una cuenta, en {@link User}.
 */
@Entity
@Table(name = "persons")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;

    @Column(name = "second_last_name", length = 100)
    private String secondLastName;

    @Convert(converter = com.aprendia.backend.security.crypto.EncryptedStringConverter.class)
    @Column(length = 255, unique = true)
    private String curp;

    @Column(name = "birth_date")
    private LocalDateTime birthDate;

    // La columna es CHAR(1) (bpchar): sin SqlTypes.CHAR, ddl-auto=validate espera VARCHAR y falla.
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 1, columnDefinition = "CHAR(1)")
    private String gender;

    @Convert(converter = com.aprendia.backend.security.crypto.EncryptedStringConverter.class)
    @Column(length = 255)
    private String phone;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(length = 100)
    private String email;

    @Embedded
    private RelativeAddress address;
}
