package com.aprendia.backend.feature.user.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record StudentDto(
    Long id,
    Integer profileId,
    String qrUrl,
    PersonDto person,
    AddressDto address,
    List<RelativeDto> relatives
) {}
