package com.aprendia.backend.feature.catalogs.service.impl;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.catalogs.dto.MetodologyRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.MetodologyResponseDTO;
import com.aprendia.backend.feature.catalogs.entities.Metodology;
import com.aprendia.backend.feature.catalogs.repository.MetodologyRepository;
import com.aprendia.backend.feature.catalogs.service.MetodologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MetodologyServiceImpl implements MetodologyService {

    @Autowired
    private MetodologyRepository metodologyRepository;

    @Override
    @Transactional
    public MetodologyResponseDTO createMetodology(MetodologyRequestDTO request) {
        Metodology metodology = Metodology.builder()
                .name(request.nombre())
                .acronym(request.sigla())
                .createdAt(LocalDateTime.now())
                .createdBy(currentUsername())
                .build();

        return mapToResponse(metodologyRepository.save(metodology));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MetodologyResponseDTO> getAllMetodologies(Pageable pageable) {
        Page<Metodology> metodologiesPage = metodologyRepository.findAll(pageable);
        return PagedResponse.fromPage(metodologiesPage.map(this::mapToResponse));
    }

    @Override
    @Transactional
    public MetodologyResponseDTO updateMetodology(Long id, MetodologyRequestDTO request) {
        Metodology metodology = metodologyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Metodología no encontrada con ID: " + id));

        metodology.setName(request.nombre());
        metodology.setAcronym(request.sigla());

        return mapToResponse(metodologyRepository.save(metodology));
    }

    @Override
    @Transactional
    public void deleteMetodology(Long id) {
        if (!metodologyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Metodología no encontrada con ID: " + id);
        }
        metodologyRepository.deleteById(id);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    private MetodologyResponseDTO mapToResponse(Metodology metodology) {
        return MetodologyResponseDTO.builder()
                .id(metodology.getId())
                .nombre(metodology.getName())
                .sigla(metodology.getAcronym())
                .createdAt(metodology.getCreatedAt() != null ? metodology.getCreatedAt().toString() : null)
                .createdBy(metodology.getCreatedBy())
                .build();
    }
}
