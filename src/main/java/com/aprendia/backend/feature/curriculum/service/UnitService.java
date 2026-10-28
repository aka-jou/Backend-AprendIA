package com.aprendia.backend.feature.curriculum.service;

import com.aprendia.backend.feature.curriculum.dto.UnitRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.UnitResponseDTO;

import java.util.List;

public interface UnitService {
    List<UnitResponseDTO> getUnits(Long idCuadernillo);
    UnitResponseDTO getUnitById(Long id);
    UnitResponseDTO createUnit(UnitRequestDTO request);
    UnitResponseDTO updateUnit(Long id, UnitRequestDTO request);
    void deleteUnit(Long id);
}
