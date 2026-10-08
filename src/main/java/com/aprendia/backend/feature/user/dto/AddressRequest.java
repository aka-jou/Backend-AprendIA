package com.aprendia.backend.feature.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AddressRequest(
    @NotBlank(message = "Street is mandatory")
    String street,

    String exteriorNumber,
    String settlementType,
    String settlement,
    Integer municipalityId,
    Integer stateId,
    String zipCode
) {}
