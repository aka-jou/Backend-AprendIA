package com.aprendia.backend.feature.catalogs.service.impl;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.catalogs.dto.MunicipalityRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.MunicipalityResponseDTO;
import com.aprendia.backend.feature.catalogs.entities.Municipality;
import com.aprendia.backend.feature.catalogs.entities.State;
import com.aprendia.backend.feature.catalogs.repository.MunicipalityRepository;
import com.aprendia.backend.feature.catalogs.repository.StateRepository;
import com.aprendia.backend.feature.catalogs.service.MunicipalityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MunicipalityServiceImpl implements MunicipalityService {

    @Autowired
    private MunicipalityRepository municipalityRepository;

    @Autowired
    private StateRepository stateRepository;

    @Override
    @Transactional
    public MunicipalityResponseDTO createMunicipality(MunicipalityRequestDTO request) {
        State state = stateRepository.findById(request.estadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado con ID: " + request.estadoId()));

        Municipality municipality = Municipality.builder()
                .name(request.nombre())
                .state(state)
                .active(request.activo())
                .createdAt(LocalDateTime.now())
                .createdBy(currentUsername())
                .build();

        return mapToResponse(municipalityRepository.save(municipality));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MunicipalityResponseDTO> getAllMunicipalities(Pageable pageable) {
        Page<Municipality> page = municipalityRepository.findAll(pageable);
        return PagedResponse.fromPage(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MunicipalityResponseDTO> getMunicipalitiesByStateId(Long stateId, Pageable pageable) {
        Page<Municipality> page = municipalityRepository.findByStateId(stateId, pageable);
        return PagedResponse.fromPage(page.map(this::mapToResponse));
    }

    @Override
    @Transactional
    public MunicipalityResponseDTO updateMunicipality(Long id, MunicipalityRequestDTO request) {
        Municipality municipality = municipalityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Municipio no encontrado con ID: " + id));

        State state = stateRepository.findById(request.estadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado con ID: " + request.estadoId()));

        municipality.setName(request.nombre());
        municipality.setState(state);
        municipality.setActive(request.activo());

        return mapToResponse(municipalityRepository.save(municipality));
    }

    @Override
    @Transactional
    public void deleteMunicipality(Long id) {
        if (!municipalityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Municipio no encontrado con ID: " + id);
        }
        municipalityRepository.deleteById(id);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    private MunicipalityResponseDTO mapToResponse(Municipality municipality) {
        return MunicipalityResponseDTO.builder()
                .id(municipality.getId())
                .nombre(municipality.getName())
                .estadoId(municipality.getState().getId())
                .estadoNombre(municipality.getState().getName())
                .activo(municipality.getActive())
                .createdAt(municipality.getCreatedAt() != null ? municipality.getCreatedAt().toString() : null)
                .createdBy(municipality.getCreatedBy())
                .build();
    }
}
