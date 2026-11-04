package com.aprendia.backend.feature.curriculum.service;

import com.aprendia.backend.feature.curriculum.dto.LayoutTypeRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.LayoutTypeResponseDTO;

import java.util.List;

public interface LayoutTypeService {
    List<LayoutTypeResponseDTO> getLayoutTypes();
    LayoutTypeResponseDTO getLayoutType(Long id);
    LayoutTypeResponseDTO createLayoutType(LayoutTypeRequestDTO request);
    LayoutTypeResponseDTO updateLayoutType(Long id, LayoutTypeRequestDTO request);
    void deleteLayoutType(Long id);
}
