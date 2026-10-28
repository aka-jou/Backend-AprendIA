package com.aprendia.backend.feature.curriculum.service.impl;

import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.catalogs.entities.LearningPath;
import com.aprendia.backend.feature.catalogs.repository.LearningPathRepository;
import com.aprendia.backend.feature.curriculum.dto.LearningPathTreeDTO;
import com.aprendia.backend.feature.curriculum.dto.LearningPathTreeDTO.TopicNode;
import com.aprendia.backend.feature.curriculum.dto.LearningPathTreeDTO.UnitNode;
import com.aprendia.backend.feature.curriculum.dto.LearningPathTreeDTO.WorkbookNode;
import com.aprendia.backend.feature.curriculum.entities.Topic;
import com.aprendia.backend.feature.curriculum.entities.Unit;
import com.aprendia.backend.feature.curriculum.entities.Workbook;
import com.aprendia.backend.feature.curriculum.repository.LearningResourceRepository;
import com.aprendia.backend.feature.curriculum.repository.TopicRepository;
import com.aprendia.backend.feature.curriculum.repository.UnitRepository;
import com.aprendia.backend.feature.curriculum.repository.WorkbookRepository;
import com.aprendia.backend.feature.curriculum.service.CurriculumSupport;
import com.aprendia.backend.feature.curriculum.service.CurriculumTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Árbol curricular de una ruta en una sola petición. Se arma con un número fijo de consultas
 * (ruta, cuadernillos, unidades, temas y conteo de recursos), sin importar el tamaño del árbol.
 */
@Service
public class CurriculumTreeServiceImpl implements CurriculumTreeService {

    @Autowired
    private LearningPathRepository learningPathRepository;

    @Autowired
    private WorkbookRepository workbookRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private LearningResourceRepository learningResourceRepository;

    @Autowired
    private CurriculumSupport support;

    @Override
    @Transactional(readOnly = true)
    public LearningPathTreeDTO getTree(Long learningPathId, boolean includeInactive) {
        boolean canManage = support.canManage();
        LearningPath learningPath = learningPathRepository.findById(learningPathId)
                .filter(path -> canManage || Boolean.TRUE.equals(path.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("Ruta de aprendizaje no encontrada con ID: " + learningPathId));

        // Los borradores solo se incluyen si se piden explícitamente y el usuario gestiona contenido
        List<Workbook> workbooks = workbookRepository.findForTree(learningPathId, includeInactive && canManage);

        List<Unit> units = workbooks.isEmpty()
                ? List.of()
                : unitRepository.findByWorkbookIds(workbooks.stream().map(Workbook::getId).toList());
        List<Topic> topics = units.isEmpty()
                ? List.of()
                : topicRepository.findByUnitIds(units.stream().map(Unit::getId).toList());
        Map<Long, Long> resourceCounts = topics.isEmpty()
                ? Map.of()
                : CurriculumSupport.toCountMap(
                        learningResourceRepository.countByTopicIds(topics.stream().map(Topic::getId).toList()));

        // Las consultas ya vienen ordenadas por (orden, id); groupingBy conserva ese orden dentro de cada grupo
        Map<Long, List<Unit>> unitsByWorkbook = units.stream()
                .collect(Collectors.groupingBy(unit -> unit.getWorkbook().getId()));
        Map<Long, List<Topic>> topicsByUnit = topics.stream()
                .collect(Collectors.groupingBy(topic -> topic.getUnit().getId()));

        List<WorkbookNode> workbookNodes = workbooks.stream()
                .map(workbook -> WorkbookNode.builder()
                        .idCuadernillo(workbook.getId())
                        .nombre(workbook.getName())
                        .descripcion(workbook.getDescription())
                        .objetivo(workbook.getObjective())
                        .autor(workbook.getAuthor())
                        .urlImagen(workbook.getImageUrl())
                        .status(workbook.getStatus())
                        .orden(workbook.getSortOrder())
                        .unidades(unitsByWorkbook.getOrDefault(workbook.getId(), List.of()).stream()
                                .map(unit -> toUnitNode(unit, topicsByUnit, resourceCounts))
                                .toList())
                        .build())
                .toList();

        return LearningPathTreeDTO.builder()
                .idPlanAprendizaje(learningPath.getId())
                .idRutaAprendizaje(learningPath.getId())
                .nombre(learningPath.getName())
                .metodologia(learningPath.getMetodology().getName())
                .perfil(learningPath.getProfile() == null ? null : learningPath.getProfile().getName())
                .urlImagen(learningPath.getImageUrl())
                .cuadernillos(workbookNodes)
                .build();
    }

    private static UnitNode toUnitNode(Unit unit, Map<Long, List<Topic>> topicsByUnit, Map<Long, Long> resourceCounts) {
        return UnitNode.builder()
                .idUnidad(unit.getId())
                .nombre(unit.getName())
                .urlImagen(unit.getImageUrl())
                .orden(unit.getSortOrder())
                .temas(topicsByUnit.getOrDefault(unit.getId(), List.of()).stream()
                        .map(topic -> TopicNode.builder()
                                .idTema(topic.getId())
                                .nombre(topic.getName())
                                .descripcion(topic.getDescription())
                                .urlImagen(topic.getImageUrl())
                                .orden(topic.getSortOrder())
                                .totalRecursos(CurriculumSupport.countFor(resourceCounts, topic.getId()))
                                .totalReactivos(CurriculumSupport.PENDING_REACTIVOS_COUNT)
                                .build())
                        .toList())
                .build();
    }
}
