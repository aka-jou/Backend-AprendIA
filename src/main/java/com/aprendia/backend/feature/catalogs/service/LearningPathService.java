package com.aprendia.backend.feature.catalogs.service;

import com.aprendia.backend.feature.catalogs.dto.LearningPathRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.LearningPathResponseDTO;

import java.util.List;

public interface LearningPathService {
    LearningPathResponseDTO createLearningPath(LearningPathRequestDTO request);
    List<LearningPathResponseDTO> getAllLearningPaths();
    LearningPathResponseDTO getLearningPathById(Long id);
    LearningPathResponseDTO updateLearningPath(Long id, LearningPathRequestDTO request);
    void deleteLearningPath(Long id);
}
