package com.aprendia.backend.feature.curriculum.service;

import com.aprendia.backend.feature.curriculum.dto.WorkbookRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.WorkbookResponseDTO;

import java.util.List;

public interface WorkbookService {
    List<WorkbookResponseDTO> getWorkbooks(Long idRutaAprendizaje, String query, Boolean status);
    WorkbookResponseDTO getWorkbookById(Long id);
    WorkbookResponseDTO createWorkbook(WorkbookRequestDTO request);
    WorkbookResponseDTO updateWorkbook(Long id, WorkbookRequestDTO request);
    void deleteWorkbook(Long id);
}
