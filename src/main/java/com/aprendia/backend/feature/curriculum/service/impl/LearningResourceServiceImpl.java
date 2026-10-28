package com.aprendia.backend.feature.curriculum.service.impl;

import com.aprendia.backend.common.security.CurrentUser;
import com.aprendia.backend.common.util.DateFormats;
import com.aprendia.backend.feature.curriculum.dto.ResourceContentDTO;
import com.aprendia.backend.feature.curriculum.dto.ResourceRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.ResourceResponseDTO;
import com.aprendia.backend.feature.curriculum.dto.ResourceSlideDTO;
import com.aprendia.backend.feature.curriculum.entities.LearningResource;
import com.aprendia.backend.feature.curriculum.entities.ResourceContent;
import com.aprendia.backend.feature.curriculum.repository.LearningResourceRepository;
import com.aprendia.backend.feature.curriculum.service.CurriculumSupport;
import com.aprendia.backend.feature.curriculum.service.LearningResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class LearningResourceServiceImpl implements LearningResourceService {

    @Autowired
    private LearningResourceRepository learningResourceRepository;

    @Autowired
    private CurriculumSupport support;

    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponseDTO> getResourcesByTopic(Long idTema) {
        support.findVisibleTopic(idTema);
        return learningResourceRepository.findByTopicId(idTema).stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceResponseDTO getResourceById(Long id) {
        return mapToResponse(support.findVisibleResource(id));
    }

    @Override
    @Transactional
    public ResourceResponseDTO createResource(ResourceRequestDTO request) {
        LearningResource resource = new LearningResource();
        apply(resource, request);
        resource.markCreated(CurrentUser.username(), LocalDateTime.now());
        return mapToResponse(learningResourceRepository.save(resource));
    }

    @Override
    @Transactional
    public ResourceResponseDTO updateResource(Long id, ResourceRequestDTO request) {
        LearningResource resource = support.findVisibleResource(id);
        apply(resource, request);
        resource.markUpdated(CurrentUser.username(), LocalDateTime.now());
        return mapToResponse(learningResourceRepository.save(resource));
    }

    @Override
    @Transactional
    public void deleteResource(Long id) {
        support.findVisibleResource(id);
        learningResourceRepository.softDelete(id, null, null, null, LocalDateTime.now(), CurrentUser.username());
    }

    private void apply(LearningResource resource, ResourceRequestDTO request) {
        resource.setTopic(support.requireTopicReference(request.idTema()));
        resource.setLayoutId(request.idLayout());
        resource.setTitle(request.titulo().trim());
        resource.setImageUrl(CurriculumSupport.blankToNull(request.urlImagen()));
        resource.setContent(toEntity(request.contenido()));
    }

    /** Las diapositivas se guardan ordenadas por "order" para que el frontend las reciba en secuencia. */
    private static ResourceContent toEntity(ResourceContentDTO content) {
        List<ResourceContent.ResourceSlide> slides = content.slides().stream()
                .sorted(Comparator.comparing(ResourceSlideDTO::order))
                .map(slide -> new ResourceContent.ResourceSlide(
                        slide.order(),
                        slide.title().trim(),
                        CurriculumSupport.blankToNull(slide.description()),
                        CurriculumSupport.blankToNull(slide.imageUrl())))
                .toList();
        return new ResourceContent(slides);
    }

    private static ResourceContentDTO toDto(ResourceContent content) {
        if (content == null || content.slides() == null) {
            return new ResourceContentDTO(List.of());
        }
        return new ResourceContentDTO(content.slides().stream()
                .map(slide -> new ResourceSlideDTO(slide.order(), slide.title(), slide.description(), slide.imageUrl()))
                .toList());
    }

    private ResourceResponseDTO mapToResponse(LearningResource resource) {
        return ResourceResponseDTO.builder()
                .idRecurso(resource.getId())
                .idTema(resource.getTopic().getId())
                .idLayout(resource.getLayoutId())
                .titulo(resource.getTitle())
                .urlImagen(resource.getImageUrl())
                .contenido(toDto(resource.getContent()))
                .createdAt(DateFormats.isoInstant(resource.getCreatedAt()))
                .updatedAt(DateFormats.isoInstant(resource.getUpdatedAt()))
                .build();
    }
}
