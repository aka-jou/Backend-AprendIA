package com.aprendia.backend.feature.curriculum.service;

import com.aprendia.backend.common.security.RoleNames;
import com.aprendia.backend.exception.BadRequestException;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.curriculum.entities.LearningResource;
import com.aprendia.backend.feature.curriculum.entities.Topic;
import com.aprendia.backend.feature.curriculum.entities.Unit;
import com.aprendia.backend.feature.curriculum.entities.Workbook;
import com.aprendia.backend.feature.curriculum.repository.LearningResourceRepository;
import com.aprendia.backend.feature.curriculum.repository.TopicRepository;
import com.aprendia.backend.feature.curriculum.repository.UnitRepository;
import com.aprendia.backend.feature.curriculum.repository.WorkbookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Reglas comunes del módulo curricular: quién gestiona contenido, qué registros son visibles y
 * cómo se resuelven las referencias.
 *
 * Visibilidad:
 * - Un registro con deleted_at no existe para la API (404).
 * - Quien no gestiona contenido (ni ADMINISTRADOR ni ASESOR_PEDAGOGICO) solo ve cuadernillos con
 *   status = true y lo que cuelga de ellos; un borrador responde 404 igual que uno inexistente.
 */
@Component
public class CurriculumSupport {

    /** Roles que pueden crear, editar y eliminar contenido curricular, y ver borradores. */
    public static final Set<String> MANAGER_AUTHORITIES = Set.of(RoleNames.ADMINISTRADOR, RoleNames.ASESOR_PEDAGOGICO);

    /** Los listados del anexo no traen conteo de reactivos en la base: el módulo de reactivos no existe aún. */
    public static final long PENDING_REACTIVOS_COUNT = 0L;

    @Autowired
    private WorkbookRepository workbookRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private LearningResourceRepository learningResourceRepository;

    public boolean canManage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(MANAGER_AUTHORITIES::contains);
    }

    // ---------- Lecturas: registro de la ruta (path variable o query param) -> 404 ----------

    public Workbook findVisibleWorkbook(Long id) {
        Workbook workbook = workbookRepository.findActiveById(id)
                .orElseThrow(() -> workbookNotFound(id));
        if (!canManage() && !Boolean.TRUE.equals(workbook.getStatus())) {
            throw workbookNotFound(id);
        }
        return workbook;
    }

    public Unit findVisibleUnit(Long id) {
        Unit unit = unitRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidad no encontrada con ID: " + id));
        if (!canManage() && !Boolean.TRUE.equals(unit.getWorkbook().getStatus())) {
            throw new ResourceNotFoundException("Unidad no encontrada con ID: " + id);
        }
        return unit;
    }

    public Topic findVisibleTopic(Long id) {
        Topic topic = topicRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema no encontrado con ID: " + id));
        if (!canManage() && !Boolean.TRUE.equals(topic.getUnit().getWorkbook().getStatus())) {
            throw new ResourceNotFoundException("Tema no encontrado con ID: " + id);
        }
        return topic;
    }

    public LearningResource findVisibleResource(Long id) {
        LearningResource resource = learningResourceRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso didáctico no encontrado con ID: " + id));
        if (!canManage() && !Boolean.TRUE.equals(resource.getTopic().getUnit().getWorkbook().getStatus())) {
            throw new ResourceNotFoundException("Recurso didáctico no encontrado con ID: " + id);
        }
        return resource;
    }

    // ---------- Referencias dentro del body -> 400 (mismo criterio que los catálogos) ----------

    public Workbook requireWorkbookReference(Long id) {
        return workbookRepository.findActiveById(id)
                .orElseThrow(() -> new BadRequestException("El cuadernillo con ID " + id + " no existe."));
    }

    public Unit requireUnitReference(Long id) {
        return unitRepository.findActiveById(id)
                .orElseThrow(() -> new BadRequestException("La unidad con ID " + id + " no existe."));
    }

    public Topic requireTopicReference(Long id) {
        return topicRepository.findActiveById(id)
                .orElseThrow(() -> new BadRequestException("El tema con ID " + id + " no existe."));
    }

    // ---------- Utilidades ----------

    /** [id, conteo] de una consulta agrupada -> mapa id -> conteo. */
    public static Map<Long, Long> toCountMap(List<Object[]> rows) {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : rows) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    public static long countFor(Map<Long, Long> counts, Long id) {
        return counts.getOrDefault(id, 0L);
    }

    public static boolean isEmpty(Collection<?> values) {
        return values == null || values.isEmpty();
    }

    /** Campo opcional: cadena vacía o solo espacios se guarda como null. */
    public static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /**
     * Texto de búsqueda -> patrón LIKE en minúsculas. Escapa !, % y _ con '!' para que el usuario
     * busque esos caracteres de forma literal (la consulta declara ESCAPE '!').
     */
    public static String toLikePattern(String query) {
        String value = blankToNull(query);
        if (value == null) {
            return null;
        }
        String escaped = value.toLowerCase(Locale.ROOT)
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
        return "%" + escaped + "%";
    }

    private static ResourceNotFoundException workbookNotFound(Long id) {
        return new ResourceNotFoundException("Cuadernillo no encontrado con ID: " + id);
    }
}
