package com.aprendia.backend.feature.catalogs.service;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.feature.catalogs.dto.StateRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.StateResponseDTO;
import org.springframework.data.domain.Pageable;

public interface StateService {
    StateResponseDTO createState(StateRequestDTO request);
    PagedResponse<StateResponseDTO> getAllStates(Pageable pageable, boolean incluirInactivos);
    StateResponseDTO updateState(Long id, StateRequestDTO request);
    StateResponseDTO updateStatus(Long id, boolean activo);
    void deleteState(Long id);
}
