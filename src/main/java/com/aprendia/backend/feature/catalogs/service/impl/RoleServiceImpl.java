package com.aprendia.backend.feature.catalogs.service.impl;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.exception.BadRequestException;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.catalogs.dto.RoleRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.RoleResponseDTO;
import com.aprendia.backend.feature.catalogs.service.RoleService;
import com.aprendia.backend.feature.catalogs.entities.Role;
import com.aprendia.backend.feature.catalogs.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Transactional
    public RoleResponseDTO createRole(RoleRequestDTO request) {
        if (roleRepository.findByName(request.nombre()).isPresent()) {
            throw new BadRequestException("Ya existe un rol con el nombre: " + request.nombre());
        }

        Role role = Role.builder()
                .name(request.nombre())
                .description(request.descripcion())
                .createdAt(LocalDateTime.now())
                .createdBy(currentUsername())
                .build();

        return mapToResponse(roleRepository.save(role));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<RoleResponseDTO> getAllRoles(Pageable pageable) {
        Page<Role> rolesPage = roleRepository.findAll(pageable);
        return PagedResponse.<RoleResponseDTO>builder()
                .content(rolesPage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))
                .pageable(new PagedResponse.PageableInfo(rolesPage.getNumber(), rolesPage.getSize()))
                .totalElements(rolesPage.getTotalElements())
                .totalPages(rolesPage.getTotalPages())
                .last(rolesPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public RoleResponseDTO updateRole(Integer id, RoleRequestDTO request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));

        roleRepository.findByName(request.nombre())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("Ya existe un rol con el nombre: " + request.nombre());
                });

        role.setName(request.nombre());
        role.setDescription(request.descripcion());

        return mapToResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public void deleteRole(Integer id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rol no encontrado con ID: " + id);
        }
        roleRepository.deleteById(id);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    private RoleResponseDTO mapToResponse(Role role) {
        return RoleResponseDTO.builder()
                .id(role.getId())
                .nombre(role.getName())
                .descripcion(role.getDescription())
                .createdAt(role.getCreatedAt() != null ? role.getCreatedAt().toString() : null)
                .createdBy(role.getCreatedBy())
                .build();
    }
}
