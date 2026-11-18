package com.aprendia.backend.feature.curriculum.service;

import com.aprendia.backend.feature.curriculum.dto.TopicRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.TopicResponseDTO;

import java.util.List;

public interface TopicService {
    List<TopicResponseDTO> getTopics(Long idUnidad, boolean incluirInactivos);
    TopicResponseDTO getTopicById(Long id);
    TopicResponseDTO createTopic(TopicRequestDTO request);
    TopicResponseDTO updateTopic(Long id, TopicRequestDTO request);
    TopicResponseDTO updateStatus(Long id, boolean activo);
    void deleteTopic(Long id);
}
