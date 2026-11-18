package com.aprendia.backend.feature.catalogs.service;

import com.aprendia.backend.feature.catalogs.dto.LearningPathRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.LearningPathResponseDTO;

import java.util.List;

public interface LearningPathService {
    LearningPathResponseDTO createLearningPath(LearningPathRequestDTO request);
    List<LearningPathResponseDTO> getAllLearningPaths(boolean incluirInactivos);
    LearningPathResponseDTO getLearningPathById(Long id);
    LearningPathResponseDTO updateLearningPath(Long id, LearningPathRequestDTO request);
    LearningPathResponseDTO updateStatus(Long id, boolean activo);
    void deleteLearningPath(Long id);
}
