package com.aprendia.backend.feature.curriculum.service.impl;

import com.aprendia.backend.common.util.DateFormats;
import com.aprendia.backend.exception.BadRequestException;
import com.aprendia.backend.exception.ConflictException;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.curriculum.dto.LayoutRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.LayoutResponseDTO;
import com.aprendia.backend.feature.curriculum.entities.Layout;
import com.aprendia.backend.feature.curriculum.repository.LayoutRepository;
import com.aprendia.backend.feature.curriculum.repository.LayoutTypeRepository;
import com.aprendia.backend.feature.curriculum.repository.LearningResourceRepository;
import com.aprendia.backend.feature.curriculum.repository.ReactivoRepository;
import com.aprendia.backend.feature.curriculum.service.CurriculumSupport;
import com.aprendia.backend.feature.curriculum.service.LayoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;

@Service
public class LayoutServiceImpl implements LayoutService {

    @Autowired
    private LayoutRepository layoutRepository;

    @Autowired
    private LayoutTypeRepository layoutTypeRepository;

    @Autowired
    private LearningResourceRepository learningResourceRepository;

    @Autowired
    private ReactivoRepository reactivoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LayoutResponseDTO> getLayouts(Long idTipoLayout, Boolean isActive) {
        return layoutRepository.search(idTipoLayout, isActive).stream().map(LayoutServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LayoutResponseDTO getLayout(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public LayoutResponseDTO createLayout(LayoutRequestDTO request) {
        String code = request.codigo().trim();
        if (layoutRepository.existsByCode(code)) {
            throw new ConflictException("Ya existe un layout con el código '" + code + "'.");
        }
        Layout layout = new Layout();
        apply(layout, request);
        return toResponse(layoutRepository.save(layout));
    }

    @Override
    @Transactional
    public LayoutResponseDTO updateLayout(Long id, LayoutRequestDTO request) {
        Layout layout = findOrThrow(id);
        String code = request.codigo().trim();
        if (layoutRepository.existsByCodeAndIdNot(code, id)) {
            throw new ConflictException("Ya existe un layout con el código '" + code + "'.");
        }
        apply(layout, request);
        return toResponse(layoutRepository.saveAndFlush(layout));
    }

    /** Borrado físico si nadie lo usa; si lo usan recursos o reactivos se pide desactivarlo. */
    @Override
    @Transactional
    public void deleteLayout(Long id) {
        Layout layout = findOrThrow(id);
        if (learningResourceRepository.existsByLayoutId(Math.toIntExact(id)) || reactivoRepository.existsByLayoutId(id)) {
            throw new ConflictException("No se puede eliminar el layout porque lo usan recursos o reactivos. "
                    + "Desactívelo con isActive=false.");
        }
        layoutRepository.delete(layout);
    }

    private Layout findOrThrow(Long id) {
        return layoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Layout no encontrado con ID: " + id));
    }

    private void apply(Layout layout, LayoutRequestDTO request) {
        layout.setLayoutType(layoutTypeRepository.findById(request.idTipoLayout())
                .orElseThrow(() -> new BadRequestException("El tipo de layout con ID " + request.idTipoLayout() + " no existe.")));
        layout.setCode(request.codigo().trim());
        layout.setName(request.nombre().trim());
        layout.setDescription(request.descripcion().trim());
        layout.setImageUrl(CurriculumSupport.blankToNull(request.urlImagen()));
        layout.setAttributes(new LinkedHashMap<>(request.atributos()));
        layout.setActive(request.isActive());
    }

    private static LayoutResponseDTO toResponse(Layout layout) {
        return LayoutResponseDTO.builder()
                .idLayout(layout.getId())
                .idTipoLayout(layout.getLayoutType().getId())
                .codigo(layout.getCode())
                .nombre(layout.getName())
                .descripcion(layout.getDescription())
                .urlImagen(layout.getImageUrl())
                .atributos(layout.getAttributes())
                .isActive(layout.getActive())
                .createdAt(DateFormats.isoInstant(layout.getCreatedAt()))
                .updatedAt(DateFormats.isoInstant(layout.getUpdatedAt()))
                .build();
    }
}
