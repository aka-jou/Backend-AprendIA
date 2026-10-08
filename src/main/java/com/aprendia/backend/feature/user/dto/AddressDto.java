package com.aprendia.backend.feature.user.dto;

import lombok.Builder;

@Builder
public record AddressDto(
    Long id,
    String street,
    String exteriorNumber,
    String settlementType,
    String settlement,
    Integer municipalityId,
    Integer stateId,
    String zipCode
) {}
