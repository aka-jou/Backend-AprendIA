package com.aprendia.backend.feature.user.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "id", nullable = false)
    private Student student;

    @Convert(converter = com.aprendia.backend.security.crypto.EncryptedStringConverter.class)
    @Column(length = 500, nullable = false)
    private String street;

    @Convert(converter = com.aprendia.backend.security.crypto.EncryptedStringConverter.class)
    @Column(name = "exterior_number", length = 255)
    private String exteriorNumber;

    @Column(name = "settlement_type", length = 100)
    private String settlementType;

    @Convert(converter = com.aprendia.backend.security.crypto.EncryptedStringConverter.class)
    @Column(length = 255)
    private String settlement;

    @Column(name = "municipality_id")
    private Integer municipalityId;

    @Column(name = "state_id")
    private Integer stateId;

    @Convert(converter = com.aprendia.backend.security.crypto.EncryptedStringConverter.class)
    @Column(name = "zip_code", length = 255)
    private String zipCode;
}
