package com.aprendia.backend.feature.catalogs.service.impl;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.exception.ConflictException;
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

@Service
public class StateServiceImpl implements StateService {

    @Autowired
    private StateRepository stateRepository;

    @Override
    @Transactional
    public StateResponseDTO createState(StateRequestDTO request) {
        State state = State.builder()
                .name(request.nombre().trim())
                .active(request.activo() == null || request.activo())
                .createdAt(LocalDateTime.now())
                .createdBy(currentUsername())
                .build();

        return mapToResponse(stateRepository.save(state));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<StateResponseDTO> getAllStates(Pageable pageable, boolean incluirInactivos) {
        Page<State> statesPage = incluirInactivos ? stateRepository.findAll(pageable) : stateRepository.findByActiveTrue(pageable);
        return PagedResponse.fromPage(statesPage.map(this::mapToResponse));
    }

    @Override
    @Transactional
    public StateResponseDTO updateState(Long id, StateRequestDTO request) {
        State state = findOrThrow(id);

        state.setName(request.nombre().trim());
        if (request.activo() != null) {
            state.setActive(request.activo());
        }

        return mapToResponse(stateRepository.save(state));
    }

    @Override
    @Transactional
    public StateResponseDTO updateStatus(Long id, boolean activo) {
        State state = findOrThrow(id);
        state.setActive(activo);
        return mapToResponse(stateRepository.save(state));
    }

    /** Solo se borra un estado sin municipios ni domicilios; si está en uso se pide desactivarlo. */
    @Override
    @Transactional
    public void deleteState(Long id) {
        State state = findOrThrow(id);
        if (stateRepository.isInUse(id)) {
            throw new ConflictException("No se puede eliminar el estado porque tiene municipios o domicilios registrados. "
                    + "Desactívelo con PATCH /catalogs/state/" + id + "/status.");
        }
        stateRepository.delete(state);
    }

    private State findOrThrow(Long id) {
        return stateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado con ID: " + id));
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
