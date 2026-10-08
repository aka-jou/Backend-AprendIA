package com.aprendia.backend.feature.user.dto;

import lombok.Builder;

@Builder
public record RelativeDto(
    PersonDto person,
    String relationship
) {}
