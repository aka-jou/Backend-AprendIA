package com.aprendia.backend.feature.user.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record PersonDto(
    Long id,
    String firstName,
    String middleName,
    String lastName,
    String secondLastName,
    String curp,
    LocalDateTime birthDate,
    String gender,
    String phone,
    String imageUrl
) {}
