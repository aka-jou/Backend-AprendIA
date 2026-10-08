package com.aprendia.backend.feature.catalogs.service;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.feature.catalogs.dto.MetodologyRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.MetodologyResponseDTO;
import org.springframework.data.domain.Pageable;

public interface MetodologyService {
    MetodologyResponseDTO createMetodology(MetodologyRequestDTO request);
    PagedResponse<MetodologyResponseDTO> getAllMetodologies(Pageable pageable);
    MetodologyResponseDTO updateMetodology(Long id, MetodologyRequestDTO request);
    void deleteMetodology(Long id);
}
