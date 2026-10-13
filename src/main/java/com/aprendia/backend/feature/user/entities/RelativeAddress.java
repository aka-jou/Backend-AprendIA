package com.aprendia.backend.feature.user.entities;

import com.aprendia.backend.security.crypto.EncryptedStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domicilio de un familiar, embebido en student_relatives (columnas address_*).
 * Mismos campos y mismo cifrado que el domicilio del estudiante ({@link Address}).
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelativeAddress {

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "address_street", length = 500)
    private String street;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "address_exterior_number", length = 255)
    private String exteriorNumber;

    @Column(name = "address_settlement_type", length = 100)
    private String settlementType;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "address_settlement", length = 255)
    private String settlement;

    @Column(name = "address_municipality_id")
    private Integer municipalityId;

    @Column(name = "address_state_id")
    private Integer stateId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "address_zip_code", length = 255)
    private String zipCode;
}
