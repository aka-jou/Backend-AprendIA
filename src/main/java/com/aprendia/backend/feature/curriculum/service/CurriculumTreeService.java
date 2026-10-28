package com.aprendia.backend.feature.curriculum.service;

import com.aprendia.backend.feature.curriculum.dto.LearningPathTreeDTO;

public interface CurriculumTreeService {
    LearningPathTreeDTO getTree(Long learningPathId, boolean includeInactive);
}
