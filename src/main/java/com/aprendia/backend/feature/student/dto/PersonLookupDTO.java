package com.aprendia.backend.feature.student.dto;

import lombok.Builder;

/** PersonLookupResult de GET /persons/by-curp/{curp}: id y personId llevan el mismo valor. */
@Builder
public record PersonLookupDTO(
    Long id,
    Long personId,
    String curp,
    String firstName,
    String middleName,
    String lastName,
    String secondLastName,
    String fullName,
    String birthDate,
    String gender,
    String phone,
    String email,
    StudentAddressDTO address
) {}
