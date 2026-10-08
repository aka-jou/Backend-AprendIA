package com.aprendia.backend.feature.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;

@Builder
public record StudentRequest(
    
    @Valid
    @NotNull(message = "Personal data is mandatory")
    PersonRequest person,

    @Valid
    @NotNull(message = "Address is mandatory")
    AddressRequest address,

    @Valid
    @NotNull(message = "Relatives list is mandatory")
    List<RelativeRequest> relatives,

    @NotNull(message = "Profile is mandatory")
    Integer profile,

    @NotBlank(message = "Email is mandatory")
    String email
) {}
