package com.aprendia.backend.feature.catalogs.service.impl;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.catalogs.dto.StateRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.StateResponseDTO;
import com.aprendia.backend.feature.catalogs.entities.State;
import com.aprendia.backend.feature.catalogs.repository.StateRepository;
import com.aprendia.backend.feature.catalogs.service.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class StateServiceImpl implements StateService {

    @Autowired
    private StateRepository stateRepository;

    @Override
    @Transactional
    public StateResponseDTO createState(StateRequestDTO request) {
        State state = State.builder()
                .name(request.nombre())
                .active(request.activo())
                .createdAt(LocalDateTime.now())
                .createdBy(currentUsername())
                .build();

        return mapToResponse(stateRepository.save(state));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<StateResponseDTO> getAllStates(Pageable pageable) {
        Page<State> statesPage = stateRepository.findAll(pageable);
        return PagedResponse.<StateResponseDTO>builder()
                .content(statesPage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))
                .pageable(new PagedResponse.PageableInfo(statesPage.getNumber(), statesPage.getSize()))
                .totalElements(statesPage.getTotalElements())
                .totalPages(statesPage.getTotalPages())
                .last(statesPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public StateResponseDTO updateState(Long id, StateRequestDTO request) {
        State state = stateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado con ID: " + id));

        state.setName(request.nombre());
        state.setActive(request.activo());

        return mapToResponse(stateRepository.save(state));
    }

    @Override
    @Transactional
    public void deleteState(Long id) {
        if (!stateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Estado no encontrado con ID: " + id);
        }
        stateRepository.deleteById(id);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    private StateResponseDTO mapToResponse(State state) {
        return StateResponseDTO.builder()
                .id(state.getId())
                .nombre(state.getName())
                .activo(state.getActive())
                .createdAt(state.getCreatedAt() != null ? state.getCreatedAt().toString() : null)
                .createdBy(state.getCreatedBy())
                .build();
    }
}
