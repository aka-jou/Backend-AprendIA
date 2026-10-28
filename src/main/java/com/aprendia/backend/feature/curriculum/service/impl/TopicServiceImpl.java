package com.aprendia.backend.feature.curriculum.service.impl;

import com.aprendia.backend.common.security.CurrentUser;
import com.aprendia.backend.common.util.DateFormats;
import com.aprendia.backend.feature.curriculum.dto.TopicRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.TopicResponseDTO;
import com.aprendia.backend.feature.curriculum.entities.Topic;
import com.aprendia.backend.feature.curriculum.repository.LearningResourceRepository;
import com.aprendia.backend.feature.curriculum.repository.TopicRepository;
import com.aprendia.backend.feature.curriculum.service.CurriculumSupport;
import com.aprendia.backend.feature.curriculum.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TopicServiceImpl implements TopicService {

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private LearningResourceRepository learningResourceRepository;

    @Autowired
    private CurriculumSupport support;

    @Override
    @Transactional(readOnly = true)
    public List<TopicResponseDTO> getTopics(Long idUnidad) {
        if (idUnidad != null) {
            support.findVisibleUnit(idUnidad);
        }
        List<Topic> topics = topicRepository.search(idUnidad, support.canManage());
        if (topics.isEmpty()) {
            return List.of();
        }
        Map<Long, Long> resourceCounts = CurriculumSupport.toCountMap(
                learningResourceRepository.countByTopicIds(topics.stream().map(Topic::getId).toList()));
        return topics.stream()
                .map(topic -> mapToResponse(topic, CurriculumSupport.countFor(resourceCounts, topic.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TopicResponseDTO getTopicById(Long id) {
        return mapToResponse(support.findVisibleTopic(id), countResources(id));
    }

    @Override
    @Transactional
    public TopicResponseDTO createTopic(TopicRequestDTO request) {
        Topic topic = new Topic();
        apply(topic, request);
        topic.setSortOrder(request.orden() != null ? request.orden() : 1);
        topic.markCreated(CurrentUser.username(), LocalDateTime.now());
        return mapToResponse(topicRepository.save(topic), 0L);
    }

    @Override
    @Transactional
    public TopicResponseDTO updateTopic(Long id, TopicRequestDTO request) {
        Topic topic = support.findVisibleTopic(id);
        apply(topic, request);
        if (request.orden() != null) {
            topic.setSortOrder(request.orden());
        }
        topic.markUpdated(CurrentUser.username(), LocalDateTime.now());
        return mapToResponse(topicRepository.save(topic), countResources(id));
    }

    /** Baja lógica del tema y, en cascada, de sus recursos. */
    @Override
    @Transactional
    public void deleteTopic(Long id) {
        support.findVisibleTopic(id);
        LocalDateTime now = LocalDateTime.now();
        String username = CurrentUser.username();
        learningResourceRepository.softDelete(null, id, null, null, now, username);
        topicRepository.softDelete(id, null, null, now, username);
    }

    private void apply(Topic topic, TopicRequestDTO request) {
        topic.setUnit(support.requireUnitReference(request.idUnidad()));
        topic.setName(request.nombre().trim());
        topic.setDescription(CurriculumSupport.blankToNull(request.descripcion()));
        topic.setImageUrl(CurriculumSupport.blankToNull(request.urlImagen()));
    }

    private long countResources(Long topicId) {
        return CurriculumSupport.countFor(
                CurriculumSupport.toCountMap(learningResourceRepository.countByTopicIds(List.of(topicId))), topicId);
    }

    private TopicResponseDTO mapToResponse(Topic topic, long totalRecursos) {
        return TopicResponseDTO.builder()
                .idTema(topic.getId())
                .idUnidad(topic.getUnit().getId())
                .nombre(topic.getName())
                .descripcion(topic.getDescription())
                .urlImagen(topic.getImageUrl())
                .orden(topic.getSortOrder())
                .totalRecursos(totalRecursos)
                .totalReactivos(CurriculumSupport.PENDING_REACTIVOS_COUNT)
                .createdAt(DateFormats.isoInstant(topic.getCreatedAt()))
                .updatedAt(DateFormats.isoInstant(topic.getUpdatedAt()))
                .build();
    }
}
