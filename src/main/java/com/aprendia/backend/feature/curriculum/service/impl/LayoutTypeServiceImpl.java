package com.aprendia.backend.feature.curriculum.service.impl;

import com.aprendia.backend.common.util.DateFormats;
import com.aprendia.backend.exception.ConflictException;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.curriculum.dto.LayoutTypeRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.LayoutTypeResponseDTO;
import com.aprendia.backend.feature.curriculum.entities.LayoutType;
import com.aprendia.backend.feature.curriculum.repository.LayoutRepository;
import com.aprendia.backend.feature.curriculum.repository.LayoutTypeRepository;
import com.aprendia.backend.feature.curriculum.service.LayoutTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LayoutTypeServiceImpl implements LayoutTypeService {

    @Autowired
    private LayoutTypeRepository layoutTypeRepository;

    @Autowired
    private LayoutRepository layoutRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LayoutTypeResponseDTO> getLayoutTypes() {
        return layoutTypeRepository.findAllByOrderByIdAsc().stream().map(LayoutTypeServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LayoutTypeResponseDTO getLayoutType(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public LayoutTypeResponseDTO createLayoutType(LayoutTypeRequestDTO request) {
        String name = request.nombre().trim();
        if (layoutTypeRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe un tipo de layout con el nombre '" + name + "'.");
        }
        LayoutType layoutType = new LayoutType();
        apply(layoutType, request);
        return toResponse(layoutTypeRepository.save(layoutType));
    }

    @Override
    @Transactional
    public LayoutTypeResponseDTO updateLayoutType(Long id, LayoutTypeRequestDTO request) {
        LayoutType layoutType = findOrThrow(id);
        String name = request.nombre().trim();
        if (layoutTypeRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Ya existe un tipo de layout con el nombre '" + name + "'.");
        }
        apply(layoutType, request);
        return toResponse(layoutTypeRepository.saveAndFlush(layoutType));
    }

    @Override
    @Transactional
    public void deleteLayoutType(Long id) {
        LayoutType layoutType = findOrThrow(id);
        if (layoutRepository.existsByLayoutTypeId(id)) {
            throw new ConflictException("No se puede eliminar el tipo de layout porque tiene layouts asociados.");
        }
        layoutTypeRepository.delete(layoutType);
    }

    private LayoutType findOrThrow(Long id) {
        return layoutTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de layout no encontrado con ID: " + id));
    }

    private static void apply(LayoutType layoutType, LayoutTypeRequestDTO request) {
        layoutType.setName(request.nombre().trim());
        layoutType.setDescription(request.descripcion().trim());
    }

    private static LayoutTypeResponseDTO toResponse(LayoutType layoutType) {
        return LayoutTypeResponseDTO.builder()
                .idTipoLayout(layoutType.getId())
                .nombre(layoutType.getName())
                .descripcion(layoutType.getDescription())
                .createdAt(DateFormats.isoInstant(layoutType.getCreatedAt()))
                .updatedAt(DateFormats.isoInstant(layoutType.getUpdatedAt()))
                .build();
    }
}
