package com.aprendia.backend.feature.curriculum.service;

import com.aprendia.backend.feature.curriculum.dto.LayoutRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.LayoutResponseDTO;

import java.util.List;

public interface LayoutService {
    List<LayoutResponseDTO> getLayouts(Long idTipoLayout, Boolean isActive);
    LayoutResponseDTO getLayout(Long id);
    LayoutResponseDTO createLayout(LayoutRequestDTO request);
    LayoutResponseDTO updateLayout(Long id, LayoutRequestDTO request);
    void deleteLayout(Long id);
}
