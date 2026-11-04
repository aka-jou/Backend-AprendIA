package com.aprendia.backend.feature.curriculum.service;

import com.aprendia.backend.feature.curriculum.dto.ReactivoRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.ReactivoResponseDTO;

import java.util.List;

public interface ReactivoService {
    List<ReactivoResponseDTO> getReactivos(Long idTema);
    ReactivoResponseDTO getReactivo(Long id);
    ReactivoResponseDTO createReactivo(ReactivoRequestDTO request);
    ReactivoResponseDTO updateReactivo(Long id, ReactivoRequestDTO request);
    void deleteReactivo(Long id);
}
