package com.aprendia.backend.feature.curriculum.service.impl;

import com.aprendia.backend.common.security.CurrentUser;
import com.aprendia.backend.common.util.DateFormats;
import com.aprendia.backend.feature.curriculum.dto.UnitRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.UnitResponseDTO;
import com.aprendia.backend.feature.curriculum.entities.Unit;
import com.aprendia.backend.feature.curriculum.repository.LearningResourceRepository;
import com.aprendia.backend.feature.curriculum.repository.ReactivoRepository;
import com.aprendia.backend.feature.curriculum.repository.TopicRepository;
import com.aprendia.backend.feature.curriculum.repository.UnitRepository;
import com.aprendia.backend.feature.curriculum.service.CurriculumSupport;
import com.aprendia.backend.feature.curriculum.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class UnitServiceImpl implements UnitService {

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private LearningResourceRepository learningResourceRepository;

    @Autowired
    private ReactivoRepository reactivoRepository;

    @Autowired
    private CurriculumSupport support;

    @Override
    @Transactional(readOnly = true)
    public List<UnitResponseDTO> getUnits(Long idCuadernillo, boolean incluirInactivos) {
        if (idCuadernillo != null) {
            support.findVisibleWorkbook(idCuadernillo);
        }
        boolean canManage = support.canManage();
        List<Unit> units = unitRepository.search(idCuadernillo, canManage, canManage && incluirInactivos);
        if (units.isEmpty()) {
            return List.of();
        }
        Map<Long, Long> topicCounts = CurriculumSupport.toCountMap(
                topicRepository.countByUnitIds(units.stream().map(Unit::getId).toList(), canManage));
        return units.stream()
                .map(unit -> mapToResponse(unit, CurriculumSupport.countFor(topicCounts, unit.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UnitResponseDTO getUnitById(Long id) {
        return mapToResponse(support.findVisibleUnit(id), countTopics(id));
    }

    @Override
    @Transactional
    public UnitResponseDTO createUnit(UnitRequestDTO request) {
        Unit unit = new Unit();
        apply(unit, request);
        unit.setSortOrder(request.orden() != null ? request.orden() : 1);
        unit.setStatus(request.status() == null || request.status());
        unit.markCreated(CurrentUser.username(), LocalDateTime.now());
        return mapToResponse(unitRepository.save(unit), 0L);
    }

    @Override
    @Transactional
    public UnitResponseDTO updateUnit(Long id, UnitRequestDTO request) {
        Unit unit = support.findVisibleUnit(id);
        apply(unit, request);
        if (request.orden() != null) {
            unit.setSortOrder(request.orden());
        }
        if (request.status() != null) {
            unit.setStatus(request.status());
        }
        unit.markUpdated(CurrentUser.username(), LocalDateTime.now());
        return mapToResponse(unitRepository.save(unit), countTopics(id));
    }

    /** Desactivar no borra: la unidad y su contenido se conservan; quien no gestiona contenido deja de verlos. */
    @Override
    @Transactional
    public UnitResponseDTO updateStatus(Long id, boolean activo) {
        Unit unit = support.findVisibleUnit(id);
        unit.setStatus(activo);
        unit.markUpdated(CurrentUser.username(), LocalDateTime.now());
        return mapToResponse(unitRepository.save(unit), countTopics(id));
    }

    /** Baja lógica de la unidad y, en cascada, de sus temas, recursos y reactivos. */
    @Override
    @Transactional
    public void deleteUnit(Long id) {
        support.findVisibleUnit(id);
        LocalDateTime now = LocalDateTime.now();
        String username = CurrentUser.username();
        learningResourceRepository.softDelete(null, null, id, null, now, username);
        reactivoRepository.softDelete(null, null, id, null, now, username);
        topicRepository.softDelete(null, id, null, now, username);
        unitRepository.softDelete(id, null, now, username);
    }

    private void apply(Unit unit, UnitRequestDTO request) {
        unit.setWorkbook(support.requireWorkbookReference(request.idCuadernillo()));
        unit.setName(request.nombre().trim());
        unit.setImageUrl(CurriculumSupport.blankToNull(request.urlImagen()));
    }

    private long countTopics(Long unitId) {
        return CurriculumSupport.countFor(
                CurriculumSupport.toCountMap(topicRepository.countByUnitIds(List.of(unitId), support.canManage())), unitId);
    }

    private UnitResponseDTO mapToResponse(Unit unit, long totalTemas) {
        return UnitResponseDTO.builder()
                .idUnidad(unit.getId())
                .idCuadernillo(unit.getWorkbook().getId())
                .nombre(unit.getName())
                .urlImagen(unit.getImageUrl())
                .orden(unit.getSortOrder())
                .totalTemas(totalTemas)
                .status(unit.getStatus())
                .createdAt(DateFormats.isoInstant(unit.getCreatedAt()))
                .updatedAt(DateFormats.isoInstant(unit.getUpdatedAt()))
                .build();
    }
}
