package com.aprendia.backend.feature.curriculum.service.impl;

import com.aprendia.backend.common.security.CurrentUser;
import com.aprendia.backend.common.util.DateFormats;
import com.aprendia.backend.exception.BadRequestException;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.catalogs.entities.LearningPath;
import com.aprendia.backend.feature.catalogs.repository.LearningPathRepository;
import com.aprendia.backend.feature.curriculum.dto.WorkbookRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.WorkbookResponseDTO;
import com.aprendia.backend.feature.curriculum.entities.Workbook;
import com.aprendia.backend.feature.curriculum.repository.LearningResourceRepository;
import com.aprendia.backend.feature.curriculum.repository.TopicRepository;
import com.aprendia.backend.feature.curriculum.repository.UnitRepository;
import com.aprendia.backend.feature.curriculum.repository.WorkbookRepository;
import com.aprendia.backend.feature.curriculum.service.CurriculumSupport;
import com.aprendia.backend.feature.curriculum.service.WorkbookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class WorkbookServiceImpl implements WorkbookService {

    @Autowired
    private WorkbookRepository workbookRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private LearningResourceRepository learningResourceRepository;

    @Autowired
    private LearningPathRepository learningPathRepository;

    @Autowired
    private CurriculumSupport support;

    @Override
    @Transactional(readOnly = true)
    public List<WorkbookResponseDTO> getWorkbooks(Long idRutaAprendizaje, String query, Boolean status) {
        if (idRutaAprendizaje != null && !learningPathRepository.existsById(idRutaAprendizaje)) {
            throw new ResourceNotFoundException("Ruta de aprendizaje no encontrada con ID: " + idRutaAprendizaje);
        }
        // Sin permisos de gestión el filtro status se ignora: solo se listan cuadernillos activos
        Boolean statusFilter = support.canManage() ? status : Boolean.TRUE;

        List<Workbook> workbooks = workbookRepository.search(idRutaAprendizaje, statusFilter, CurriculumSupport.toLikePattern(query));
        if (workbooks.isEmpty()) {
            return List.of();
        }
        Map<Long, Long> unitCounts = CurriculumSupport.toCountMap(
                unitRepository.countByWorkbookIds(workbooks.stream().map(Workbook::getId).toList()));
        return workbooks.stream()
                .map(workbook -> mapToResponse(workbook, CurriculumSupport.countFor(unitCounts, workbook.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkbookResponseDTO getWorkbookById(Long id) {
        Workbook workbook = support.findVisibleWorkbook(id);
        return mapToResponse(workbook, countUnits(id));
    }

    @Override
    @Transactional
    public WorkbookResponseDTO createWorkbook(WorkbookRequestDTO request) {
        Workbook workbook = new Workbook();
        apply(workbook, request);
        workbook.setSortOrder(request.orden() != null ? request.orden() : 1);
        workbook.markCreated(CurrentUser.username(), LocalDateTime.now());
        return mapToResponse(workbookRepository.save(workbook), 0L);
    }

    @Override
    @Transactional
    public WorkbookResponseDTO updateWorkbook(Long id, WorkbookRequestDTO request) {
        Workbook workbook = support.findVisibleWorkbook(id);
        apply(workbook, request);
        if (request.orden() != null) {
            workbook.setSortOrder(request.orden());
        }
        workbook.markUpdated(CurrentUser.username(), LocalDateTime.now());
        return mapToResponse(workbookRepository.save(workbook), countUnits(id));
    }

    /**
     * Baja lógica en cascada: el cuadernillo y todas sus unidades, temas y recursos reciben la misma
     * marca deleted_at. Nada se borra físicamente.
     */
    @Override
    @Transactional
    public void deleteWorkbook(Long id) {
        support.findVisibleWorkbook(id);
        LocalDateTime now = LocalDateTime.now();
        String username = CurrentUser.username();
        learningResourceRepository.softDelete(null, null, null, id, now, username);
        topicRepository.softDelete(null, null, id, now, username);
        unitRepository.softDelete(null, id, now, username);
        workbookRepository.softDeleteById(id, now, username);
    }

    private void apply(Workbook workbook, WorkbookRequestDTO request) {
        workbook.setLearningPath(findLearningPath(request.idRutaAprendizaje()));
        workbook.setName(request.nombre().trim());
        workbook.setDescription(request.descripcion().trim());
        workbook.setObjective(request.objetivo().trim());
        workbook.setAuthor(request.autor().trim());
        workbook.setImageUrl(request.urlImagen().trim());
        workbook.setStatus(request.status());
    }

    private LearningPath findLearningPath(Long idRutaAprendizaje) {
        return learningPathRepository.findById(idRutaAprendizaje)
                .orElseThrow(() -> new BadRequestException("La ruta de aprendizaje con ID " + idRutaAprendizaje + " no existe en el catálogo."));
    }

    private long countUnits(Long workbookId) {
        return CurriculumSupport.countFor(
                CurriculumSupport.toCountMap(unitRepository.countByWorkbookIds(List.of(workbookId))), workbookId);
    }

    private WorkbookResponseDTO mapToResponse(Workbook workbook, long totalUnidades) {
        LearningPath learningPath = workbook.getLearningPath();
        return WorkbookResponseDTO.builder()
                .idCuadernillo(workbook.getId())
                .idRutaAprendizaje(learningPath.getId())
                .nombreRuta(learningPath.getName())
                .nombre(workbook.getName())
                .descripcion(workbook.getDescription())
                .objetivo(workbook.getObjective())
                .autor(workbook.getAuthor())
                .urlImagen(workbook.getImageUrl())
                .status(workbook.getStatus())
                .orden(workbook.getSortOrder())
                .totalUnidades(totalUnidades)
                .createdAt(DateFormats.isoInstant(workbook.getCreatedAt()))
                .updatedAt(DateFormats.isoInstant(workbook.getUpdatedAt()))
                .build();
    }
}
