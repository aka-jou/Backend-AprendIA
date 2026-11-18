package com.aprendia.backend.feature.catalogs.service.impl;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.common.security.RoleNames;
import com.aprendia.backend.exception.BadRequestException;
import com.aprendia.backend.exception.ConflictException;
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
import java.util.Set;

@Service
public class RoleServiceImpl implements RoleService {

    /**
     * Roles oficiales del sistema. No se pueden desactivar ni eliminar: la seguridad (@PreAuthorize)
     * y el alta de alumnos dependen de ellos.
     */
    public static final Set<String> OFFICIAL_ROLES = Set.of(
            RoleNames.ADMINISTRADOR, RoleNames.DOCENTE_AULA, "ROLE_ENLACE_INSTITUCIONAL", RoleNames.STUDENT);

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
                .status(request.status() == null || request.status())
                .createdAt(LocalDateTime.now())
                .createdBy(currentUsername())
                .build();

        return mapToResponse(roleRepository.save(role));
    }

    /** Sin incluirInactivos solo se listan los roles activos. */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<RoleResponseDTO> getAllRoles(Pageable pageable, boolean incluirInactivos) {
        Page<Role> rolesPage = incluirInactivos ? roleRepository.findAll(pageable) : roleRepository.findByStatusTrue(pageable);
        return PagedResponse.fromPage(rolesPage.map(this::mapToResponse));
    }

    @Override
    @Transactional
    public RoleResponseDTO updateRole(Integer id, RoleRequestDTO request) {
        Role role = findOrThrow(id);

        roleRepository.findByName(request.nombre())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("Ya existe un rol con el nombre: " + request.nombre());
                });
        if (OFFICIAL_ROLES.contains(role.getName()) && !role.getName().equals(request.nombre())) {
            throw new ConflictException("No se puede renombrar el rol oficial " + role.getName() + ".");
        }

        role.setName(request.nombre());
        role.setDescription(request.descripcion());
        if (request.status() != null) {
            applyStatus(role, request.status());
        }

        return mapToResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponseDTO updateStatus(Integer id, boolean activo) {
        Role role = findOrThrow(id);
        applyStatus(role, activo);
        return mapToResponse(roleRepository.save(role));
    }

    /** Solo se borra un rol que nadie tiene asignado; si está en uso se pide desactivarlo. */
    @Override
    @Transactional
    public void deleteRole(Integer id) {
        Role role = findOrThrow(id);
        if (OFFICIAL_ROLES.contains(role.getName())) {
            throw new ConflictException("No se puede eliminar el rol oficial " + role.getName() + ".");
        }
        if (roleRepository.isAssigned(id)) {
            throw new ConflictException("No se puede eliminar el rol porque hay usuarios que lo tienen asignado. "
                    + "Desactívelo con PATCH /catalogs/roles/" + id + "/status.");
        }
        roleRepository.delete(role);
    }

    private void applyStatus(Role role, boolean activo) {
        if (!activo && OFFICIAL_ROLES.contains(role.getName())) {
            throw new ConflictException("No se puede desactivar el rol oficial " + role.getName() + ".");
        }
        role.setStatus(activo);
    }

    private Role findOrThrow(Integer id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));
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
                .status(role.getStatus())
                .createdAt(role.getCreatedAt() != null ? role.getCreatedAt().toString() : null)
                .createdBy(role.getCreatedBy())
                .build();
    }
}
