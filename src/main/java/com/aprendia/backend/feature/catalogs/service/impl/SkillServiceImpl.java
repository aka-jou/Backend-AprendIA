package com.aprendia.backend.feature.catalogs.service.impl;

import com.aprendia.backend.exception.BadRequestException;
import com.aprendia.backend.exception.ConflictException;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.catalogs.dto.SkillRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.SkillResponseDTO;
import com.aprendia.backend.feature.catalogs.entities.Skill;
import com.aprendia.backend.feature.catalogs.entities.SkillCategory;
import com.aprendia.backend.feature.catalogs.repository.SkillCategoryRepository;
import com.aprendia.backend.feature.catalogs.repository.SkillRepository;
import com.aprendia.backend.feature.catalogs.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SkillServiceImpl implements SkillService {

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private SkillCategoryRepository skillCategoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponseDTO> getSkills(Boolean status) {
        return skillRepository.search(status).stream().map(SkillServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SkillResponseDTO getSkill(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public SkillResponseDTO createSkill(SkillRequestDTO request) {
        String name = request.nombre().trim();
        if (skillRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe una habilidad con el nombre '" + name + "'.");
        }
        Skill skill = new Skill();
        apply(skill, request);
        return toResponse(skillRepository.save(skill));
    }

    @Override
    @Transactional
    public SkillResponseDTO updateSkill(Long id, SkillRequestDTO request) {
        Skill skill = findOrThrow(id);
        String name = request.nombre().trim();
        if (skillRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Ya existe una habilidad con el nombre '" + name + "'.");
        }
        apply(skill, request);
        return toResponse(skillRepository.saveAndFlush(skill));
    }

    /** Borrado físico si ningún reactivo la usa; si la usan se pide desactivarla. */
    @Override
    @Transactional
    public void deleteSkill(Long id) {
        Skill skill = findOrThrow(id);
        if (skillRepository.isUsedByReactivos(id)) {
            throw new ConflictException("No se puede eliminar la habilidad porque la usan reactivos. Desactívela con status=false.");
        }
        skillRepository.delete(skill);
    }

    private Skill findOrThrow(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habilidad no encontrada con ID: " + id));
    }

    private void apply(Skill skill, SkillRequestDTO request) {
        skill.setName(request.nombre().trim());
        skill.setDescription(request.descripcion() == null || request.descripcion().isBlank() ? null : request.descripcion().trim());
        SkillCategory category = null;
        if (request.idCategoria() != null) {
            category = skillCategoryRepository.findById(request.idCategoria())
                    .orElseThrow(() -> new BadRequestException("La categoría de habilidad con ID " + request.idCategoria() + " no existe."));
        }
        skill.setCategory(category);
        // status es opcional: por defecto true al crear; en PUT, si se omite, se conserva
        if (request.status() != null) {
            skill.setStatus(request.status());
        } else if (skill.getStatus() == null) {
            skill.setStatus(Boolean.TRUE);
        }
    }

    private static SkillResponseDTO toResponse(Skill skill) {
        SkillCategory category = skill.getCategory();
        return SkillResponseDTO.builder()
                .id(skill.getId())
                .idHabilidad(skill.getId())
                .nombre(skill.getName())
                .descripcion(skill.getDescription())
                .idCategoria(category != null ? category.getId() : null)
                .nombreCategoria(category != null ? category.getName() : null)
                .status(skill.getStatus())
                .build();
    }
}
