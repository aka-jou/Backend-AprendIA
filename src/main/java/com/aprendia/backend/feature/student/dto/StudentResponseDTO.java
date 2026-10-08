package com.aprendia.backend.feature.student.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/** Expediente del estudiante (forma de la sección 6.3 de la spec, más qrCode para el login por QR). */
@Builder
public record StudentResponseDTO(
    Long id,
    Person person,
    Address address,
    List<StudentRelativeDTO> relatives,
    Long profile,
    String profileName,
    String email,
    @JsonProperty("isActive") Boolean isActive,
    String qrCode,
    String createdAt
) {
    @Builder
    public record Person(
        String firstName,
        String middleName,
        String lastName,
        String secondLastName,
        String curp,
        String birthDate,
        String gender,
        String phone,
        String imageUrl
    ) {}

    @Builder
    public record Address(
        String street,
        String exteriorNumber,
        String settlementType,
        String settlement,
        Long municipalityId,
        Long stateId,
        String zipCode
    ) {}
}
