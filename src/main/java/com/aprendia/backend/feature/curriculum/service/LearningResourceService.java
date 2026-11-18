package com.aprendia.backend.feature.curriculum.service;

import com.aprendia.backend.feature.curriculum.dto.ResourceRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.ResourceResponseDTO;

import java.util.List;

public interface LearningResourceService {
    List<ResourceResponseDTO> getResourcesByTopic(Long idTema, boolean incluirInactivos);
    ResourceResponseDTO getResourceById(Long id);
    ResourceResponseDTO createResource(ResourceRequestDTO request);
    ResourceResponseDTO updateResource(Long id, ResourceRequestDTO request);
    ResourceResponseDTO updateStatus(Long id, boolean activo);
    void deleteResource(Long id);
}
