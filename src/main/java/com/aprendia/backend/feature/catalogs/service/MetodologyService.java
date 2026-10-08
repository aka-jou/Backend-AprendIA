package com.aprendia.backend.feature.catalogs.service;

import com.aprendia.backend.feature.catalogs.dto.MetodologyRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.MetodologyResponseDTO;

import java.util.List;

public interface MetodologyService {
    MetodologyResponseDTO createMetodology(MetodologyRequestDTO request);
    List<MetodologyResponseDTO> getAllMetodologies();
    MetodologyResponseDTO getMetodologyById(Long id);
    MetodologyResponseDTO updateMetodology(Long id, MetodologyRequestDTO request);
    void deleteMetodology(Long id);
}
