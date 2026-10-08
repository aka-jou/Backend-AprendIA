package com.aprendia.backend.feature.catalogs.service;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.feature.catalogs.dto.StateRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.StateResponseDTO;
import org.springframework.data.domain.Pageable;

public interface StateService {
    StateResponseDTO createState(StateRequestDTO request);
    PagedResponse<StateResponseDTO> getAllStates(Pageable pageable);
    StateResponseDTO updateState(Long id, StateRequestDTO request);
    void deleteState(Long id);
}
