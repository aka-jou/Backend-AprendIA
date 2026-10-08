package com.aprendia.backend.feature.catalogs.service.impl;

import com.aprendia.backend.common.security.CurrentUser;
import com.aprendia.backend.exception.ConflictException;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.catalogs.dto.MetodologyRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.MetodologyResponseDTO;
import com.aprendia.backend.feature.catalogs.entities.Metodology;
import com.aprendia.backend.feature.catalogs.repository.LearningPathRepository;
import com.aprendia.backend.feature.catalogs.repository.MetodologyRepository;
import com.aprendia.backend.feature.catalogs.service.MetodologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MetodologyServiceImpl implements MetodologyService {

    @Autowired
    private MetodologyRepository metodologyRepository;

    @Autowired
    private LearningPathRepository learningPathRepository;

    @Override
    @Transactional
    public MetodologyResponseDTO createMetodology(MetodologyRequestDTO request) {
        Metodology metodology = Metodology.builder()
                .name(request.nombre().trim())
                .author(blankToNull(request.autor()))
                .status(request.status())
                .createdAt(LocalDateTime.now())
                .createdBy(CurrentUser.username())
                .build();
        return mapToResponse(metodologyRepository.save(metodology));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetodologyResponseDTO> getAllMetodologies() {
        return metodologyRepository.findAllByOrderByIdAsc().stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MetodologyResponseDTO getMetodologyById(Long id) {
        return mapToResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public MetodologyResponseDTO updateMetodology(Long id, MetodologyRequestDTO request) {
        Metodology metodology = findOrThrow(id);
        metodology.setName(request.nombre().trim());
        metodology.setAuthor(blankToNull(request.autor()));
        metodology.setStatus(request.status());
        return mapToResponse(metodologyRepository.save(metodology));
    }

    @Override
    @Transactional
    public void deleteMetodology(Long id) {
        findOrThrow(id);
        if (learningPathRepository.existsByMetodologyId(id)) {
            throw new ConflictException("No se puede eliminar la metodología porque tiene rutas de aprendizaje asociadas. Desactívela con status=false.");
        }
        metodologyRepository.deleteById(id);
    }

    private Metodology findOrThrow(Long id) {
        return metodologyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Metodología no encontrada con ID: " + id));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private MetodologyResponseDTO mapToResponse(Metodology metodology) {
        return MetodologyResponseDTO.builder()
                .id(metodology.getId())
                .nombre(metodology.getName())
                .autor(metodology.getAuthor())
                .status(metodology.getStatus())
                .build();
    }
}
