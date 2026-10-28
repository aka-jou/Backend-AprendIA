package com.aprendia.backend.feature.curriculum.service;

import com.aprendia.backend.feature.curriculum.dto.TopicRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.TopicResponseDTO;

import java.util.List;

public interface TopicService {
    List<TopicResponseDTO> getTopics(Long idUnidad);
    TopicResponseDTO getTopicById(Long id);
    TopicResponseDTO createTopic(TopicRequestDTO request);
    TopicResponseDTO updateTopic(Long id, TopicRequestDTO request);
    void deleteTopic(Long id);
}
