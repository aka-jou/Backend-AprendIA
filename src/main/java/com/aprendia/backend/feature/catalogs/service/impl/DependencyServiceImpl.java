package com.aprendia.backend.feature.catalogs.service.impl;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.exception.ConflictException;
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
                .name(request.nombre().trim())
                .internalCode(request.codigoInterno().trim())
                .type(blankToNull(request.tipo()))
                .status(request.status() == null || request.status())
                .createdAt(LocalDateTime.now())
                .createdBy(currentUsername())
                .build();

        return mapToResponse(dependencyRepository.save(dependency));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DependencyResponseDTO> getAllDependencies(Pageable pageable, boolean incluirInactivos) {
        Page<Dependency> dependenciesPage = incluirInactivos
                ? dependencyRepository.findAll(pageable)
                : dependencyRepository.findByStatusTrue(pageable);
        return PagedResponse.fromPage(dependenciesPage.map(this::mapToResponse));
    }

    @Override
    @Transactional
    public DependencyResponseDTO updateDependency(Long id, DependencyRequestDTO request) {
        Dependency dependency = findOrThrow(id);

        dependency.setName(request.nombre().trim());
        dependency.setInternalCode(request.codigoInterno().trim());
        dependency.setType(blankToNull(request.tipo()));
        if (request.status() != null) {
            dependency.setStatus(request.status());
        }

        return mapToResponse(dependencyRepository.save(dependency));
    }

    @Override
    @Transactional
    public DependencyResponseDTO updateStatus(Long id, boolean activo) {
        Dependency dependency = findOrThrow(id);
        dependency.setStatus(activo);
        return mapToResponse(dependencyRepository.save(dependency));
    }

    /** Solo se borra una dependencia sin usuarios adscritos; si los tiene se pide desactivarla. */
    @Override
    @Transactional
    public void deleteDependency(Long id) {
        Dependency dependency = findOrThrow(id);
        if (dependencyRepository.hasUsers(id)) {
            throw new ConflictException("No se puede eliminar la dependencia porque tiene usuarios adscritos. "
                    + "Desactívela con PATCH /catalogs/dependencies/" + id + "/status.");
        }
        dependencyRepository.delete(dependency);
    }

    private Dependency findOrThrow(Long id) {
        return dependencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dependencia no encontrada con ID: " + id));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    private DependencyResponseDTO mapToResponse(Dependency dependency) {
        return DependencyResponseDTO.builder()
                .id(dependency.getId())
                .nombre(dependency.getName())
                .tipo(dependency.getType())
                .codigoInterno(dependency.getInternalCode())
                .status(dependency.getStatus())
                .createdAt(dependency.getCreatedAt() != null ? dependency.getCreatedAt().toString() : null)
                .createdBy(dependency.getCreatedBy())
                .build();
    }
}
