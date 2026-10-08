package com.aprendia.backend.feature.catalogs.service;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.feature.catalogs.dto.DependencyRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.DependencyResponseDTO;
import org.springframework.data.domain.Pageable;

public interface DependencyService {
    DependencyResponseDTO createDependency(DependencyRequestDTO request);
    PagedResponse<DependencyResponseDTO> getAllDependencies(Pageable pageable);
    DependencyResponseDTO updateDependency(Long id, DependencyRequestDTO request);
    void deleteDependency(Long id);
}
