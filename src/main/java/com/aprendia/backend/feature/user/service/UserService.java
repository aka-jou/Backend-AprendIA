package com.aprendia.backend.feature.user.service;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.feature.user.dto.UserCreateRequestDTO;
import com.aprendia.backend.feature.user.dto.UserDetailDTO;
import com.aprendia.backend.feature.user.dto.UserSummaryDTO;
import com.aprendia.backend.feature.user.dto.UserUpdateRequestDTO;
import org.springframework.data.domain.Pageable;

/** Módulo 2 de la Especificación v2.0: personal docente, asesores, supervisores y administradores. */
public interface UserService {
    PagedResponse<UserSummaryDTO> getUsers(String query, Pageable pageable);
    UserDetailDTO getUserById(Long id);
    UserDetailDTO createUser(UserCreateRequestDTO request);
    UserDetailDTO updateUser(Long id, UserUpdateRequestDTO request);
    void deactivateUser(Long id);
}
