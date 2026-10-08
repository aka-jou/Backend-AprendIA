package com.aprendia.backend.feature.user.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Column(length = 1, columnDefinition = "CHAR(1)")
    private String gender;

    @Convert(converter = com.aprendia.backend.security.crypto.EncryptedStringConverter.class)
    @Column(length = 255)
    private String phone;

    @Column(name = "image_url")
    private String imageUrl;
}
