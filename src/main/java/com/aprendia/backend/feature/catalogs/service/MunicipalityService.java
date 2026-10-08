package com.aprendia.backend.feature.catalogs.service;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.feature.catalogs.dto.MunicipalityRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.MunicipalityResponseDTO;
import org.springframework.data.domain.Pageable;

public interface MunicipalityService {
    MunicipalityResponseDTO createMunicipality(MunicipalityRequestDTO request);
    PagedResponse<MunicipalityResponseDTO> getAllMunicipalities(Pageable pageable);
    PagedResponse<MunicipalityResponseDTO> getMunicipalitiesByStateId(Long stateId, Pageable pageable);
    MunicipalityResponseDTO updateMunicipality(Long id, MunicipalityRequestDTO request);
    void deleteMunicipality(Long id);
}
