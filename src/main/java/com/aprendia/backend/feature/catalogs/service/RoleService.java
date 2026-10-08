package com.aprendia.backend.feature.catalogs.service;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.feature.catalogs.dto.RoleRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.RoleResponseDTO;
import org.springframework.data.domain.Pageable;

public interface RoleService {
    RoleResponseDTO createRole(RoleRequestDTO request);
    PagedResponse<RoleResponseDTO> getAllRoles(Pageable pageable);
    RoleResponseDTO updateRole(Integer id, RoleRequestDTO request);
    void deleteRole(Integer id);
}
