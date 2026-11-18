package com.aprendia.backend.feature.catalogs.service;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.feature.catalogs.dto.DependencyRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.DependencyResponseDTO;
import org.springframework.data.domain.Pageable;

public interface DependencyService {
    DependencyResponseDTO createDependency(DependencyRequestDTO request);
    PagedResponse<DependencyResponseDTO> getAllDependencies(Pageable pageable, boolean incluirInactivos);
    DependencyResponseDTO updateDependency(Long id, DependencyRequestDTO request);
    DependencyResponseDTO updateStatus(Long id, boolean activo);
    void deleteDependency(Long id);
}
