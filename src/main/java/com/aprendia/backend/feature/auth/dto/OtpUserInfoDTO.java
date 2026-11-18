package com.aprendia.backend.feature.auth.dto;

import com.aprendia.backend.common.security.RoleNames;
import com.aprendia.backend.feature.user.entities.Person;
import com.aprendia.backend.feature.user.entities.User;
import lombok.Builder;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Datos del usuario que acompañan al token (POST /auth/otp/verify y POST /auth/admin). */
@Builder
public record OtpUserInfoDTO(
    Long idUsuario,
    String nombre,
    String username,
    List<String> roles,
    Long idEntidad
) {
    public static OtpUserInfoDTO from(User user) {
        Person person = user.getPerson();
        String nombre = user.getUsername();
        if (person != null) {
            nombre = Stream.of(person.getFirstName(), person.getMiddleName(), person.getLastName(), person.getSecondLastName())
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(part -> !part.isEmpty())
                    .collect(Collectors.joining(" "));
        }
        return OtpUserInfoDTO.builder()
                .idUsuario(user.getId())
                .nombre(nombre)
                .username(user.getUsername())
                .roles(user.getRoles().stream().map(role -> RoleNames.toApi(role.getName())).toList())
                // idEntidad = dependencia de adscripción del usuario (users.dependency_id)
                .idEntidad(user.getDependency() != null ? user.getDependency().getId() : null)
                .build();
    }
}
