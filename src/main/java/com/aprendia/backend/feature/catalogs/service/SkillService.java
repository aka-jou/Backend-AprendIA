package com.aprendia.backend.feature.catalogs.service;

import com.aprendia.backend.feature.catalogs.dto.SkillRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.SkillResponseDTO;

import java.util.List;

public interface SkillService {
    List<SkillResponseDTO> getSkills(Boolean status);
    SkillResponseDTO getSkill(Long id);
    SkillResponseDTO createSkill(SkillRequestDTO request);
    SkillResponseDTO updateSkill(Long id, SkillRequestDTO request);
    void deleteSkill(Long id);
}
