package com.aprendia.backend.feature.catalogs.service;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.feature.catalogs.dto.MunicipalityRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.MunicipalityResponseDTO;
import org.springframework.data.domain.Pageable;

public interface MunicipalityService {
    MunicipalityResponseDTO createMunicipality(MunicipalityRequestDTO request);
    PagedResponse<MunicipalityResponseDTO> getAllMunicipalities(Pageable pageable, boolean incluirInactivos);
    PagedResponse<MunicipalityResponseDTO> getMunicipalitiesByStateId(Long stateId, Pageable pageable, boolean incluirInactivos);
    MunicipalityResponseDTO updateMunicipality(Long id, MunicipalityRequestDTO request);
    MunicipalityResponseDTO updateStatus(Long id, boolean activo);
    void deleteMunicipality(Long id);
}
