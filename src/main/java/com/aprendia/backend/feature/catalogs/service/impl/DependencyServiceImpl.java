package com.aprendia.backend.feature.catalogs.service.impl;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.catalogs.dto.DependencyRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.DependencyResponseDTO;
import com.aprendia.backend.feature.catalogs.entities.Dependency;
import com.aprendia.backend.feature.catalogs.repository.DependencyRepository;
import com.aprendia.backend.feature.catalogs.service.DependencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DependencyServiceImpl implements DependencyService {

    @Autowired
    private DependencyRepository dependencyRepository;

    @Override
    @Transactional
    public DependencyResponseDTO createDependency(DependencyRequestDTO request) {
        Dependency dependency = Dependency.builder()
                .name(request.nombre())
                .internalCode(request.codigoInterno())
                .createdAt(LocalDateTime.now())
                .createdBy(currentUsername())
                .build();

        return mapToResponse(dependencyRepository.save(dependency));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DependencyResponseDTO> getAllDependencies(Pageable pageable) {
        Page<Dependency> dependenciesPage = dependencyRepository.findAll(pageable);
        return PagedResponse.fromPage(dependenciesPage.map(this::mapToResponse));
    }

    @Override
    @Transactional
    public DependencyResponseDTO updateDependency(Long id, DependencyRequestDTO request) {
        Dependency dependency = dependencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dependencia no encontrada con ID: " + id));

        dependency.setName(request.nombre());
        dependency.setInternalCode(request.codigoInterno());

        return mapToResponse(dependencyRepository.save(dependency));
    }

    @Override
    @Transactional
    public void deleteDependency(Long id) {
        if (!dependencyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dependencia no encontrada con ID: " + id);
        }
        dependencyRepository.deleteById(id);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    private DependencyResponseDTO mapToResponse(Dependency dependency) {
        return DependencyResponseDTO.builder()
                .id(dependency.getId())
                .nombre(dependency.getName())
                .codigoInterno(dependency.getInternalCode())
                .createdAt(dependency.getCreatedAt() != null ? dependency.getCreatedAt().toString() : null)
                .createdBy(dependency.getCreatedBy())
                .build();
    }
}
