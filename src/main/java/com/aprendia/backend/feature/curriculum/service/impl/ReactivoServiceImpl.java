package com.aprendia.backend.feature.curriculum.service.impl;

import com.aprendia.backend.common.security.CurrentUser;
import com.aprendia.backend.exception.BadRequestException;
import com.aprendia.backend.feature.catalogs.entities.Skill;
import com.aprendia.backend.feature.catalogs.repository.SkillRepository;
import com.aprendia.backend.feature.curriculum.dto.ExerciseRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.ReactivoRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.ReactivoResponseDTO;
import com.aprendia.backend.feature.curriculum.dto.ReactivoSkillRequestDTO;
import com.aprendia.backend.feature.curriculum.entities.Exercise;
import com.aprendia.backend.feature.curriculum.entities.Layout;
import com.aprendia.backend.feature.curriculum.entities.Reactivo;
import com.aprendia.backend.feature.curriculum.entities.ReactivoPreviousSkill;
import com.aprendia.backend.feature.curriculum.entities.ReactivoSkill;
import com.aprendia.backend.feature.curriculum.repository.ExerciseRepository;
import com.aprendia.backend.feature.curriculum.repository.LayoutRepository;
import com.aprendia.backend.feature.curriculum.repository.ReactivoRepository;
import com.aprendia.backend.feature.curriculum.service.CurriculumSupport;
import com.aprendia.backend.feature.curriculum.service.ExerciseConfigValidator;
import com.aprendia.backend.feature.curriculum.service.ReactivoMapper;
import com.aprendia.backend.feature.curriculum.service.ReactivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reactivos con sus habilidades y ejercicios. POST y PUT reciben el reactivo completo, como lo arma
 * el modal del frontend (Paso 1: datos generales y habilidades; Paso 2: ejercicios).
 *
 * En PUT, exercises[] es la lista completa: un ejercicio que trae "id" se actualiza y conserva su ID;
 * uno sin "id" se crea; los que ya no vienen se dan de baja. Así la app móvil no pierde la referencia
 * a los ejercicios que no cambiaron.
 */
@Service
public class ReactivoServiceImpl implements ReactivoService {

    private static final BigDecimal TOTAL_WEIGHT = new BigDecimal("100");

    @Autowired
    private ReactivoRepository reactivoRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private LayoutRepository layoutRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private CurriculumSupport support;

    @Autowired
    private ReactivoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReactivoResponseDTO> getReactivos(Long idTema) {
        if (idTema != null) {
            support.findVisibleTopic(idTema);
        }
        return mapper.toResponses(reactivoRepository.search(idTema, support.canManage()));
    }

    @Override
    @Transactional(readOnly = true)
    public ReactivoResponseDTO getReactivo(Long id) {
        return mapper.toResponse(support.findVisibleReactivo(id));
    }

    @Override
    @Transactional
    public ReactivoResponseDTO createReactivo(ReactivoRequestDTO request) {
        Reactivo reactivo = new Reactivo();
        Layout layout = apply(reactivo, request);
        validateExercises(layout, request.exercises(), Map.of());

        LocalDateTime now = LocalDateTime.now();
        String username = CurrentUser.username();
        reactivo.markCreated(username, now);
        reactivo = reactivoRepository.save(reactivo);

        List<Exercise> exercises = new ArrayList<>();
        for (ExerciseRequestDTO dto : request.exercises()) {
            Exercise exercise = new Exercise();
            exercise.setReactivo(reactivo);
            applyExercise(exercise, dto);
            exercise.markCreated(username, now);
            exercises.add(exercise);
        }
        exerciseRepository.saveAll(exercises);
        return mapper.toResponse(reactivo);
    }

    @Override
    @Transactional
    public ReactivoResponseDTO updateReactivo(Long id, ReactivoRequestDTO request) {
        Reactivo reactivo = support.findVisibleReactivo(id);
        Map<Long, Exercise> current = exerciseRepository.findByReactivoIds(List.of(id)).stream()
                .collect(Collectors.toMap(Exercise::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        Layout layout = apply(reactivo, request);
        Map<Integer, Exercise> matched = validateExercises(layout, request.exercises(), current);

        LocalDateTime now = LocalDateTime.now();
        String username = CurrentUser.username();
        reactivo.markUpdated(username, now);
        reactivo = reactivoRepository.save(reactivo);

        List<Exercise> toSave = new ArrayList<>();
        Set<Long> kept = new HashSet<>();
        for (int i = 0; i < request.exercises().size(); i++) {
            ExerciseRequestDTO dto = request.exercises().get(i);
            Exercise exercise = matched.get(i);
            if (exercise == null) {
                exercise = new Exercise();
                exercise.setReactivo(reactivo);
                exercise.markCreated(username, now);
            } else {
                kept.add(exercise.getId());
                exercise.markUpdated(username, now);
            }
            applyExercise(exercise, dto);
            toSave.add(exercise);
        }
        for (Exercise old : current.values()) {
            if (!kept.contains(old.getId())) {
                old.markDeleted(username, now);
                toSave.add(old);
            }
        }
        exerciseRepository.saveAll(toSave);
        return mapper.toResponse(reactivo);
    }

    /** Baja lógica del reactivo y de todos sus ejercicios. */
    @Override
    @Transactional
    public void deleteReactivo(Long id) {
        support.findVisibleReactivo(id);
        LocalDateTime now = LocalDateTime.now();
        String username = CurrentUser.username();
        exerciseRepository.softDeleteByReactivo(id, now, username);
        reactivoRepository.softDelete(id, null, null, null, now, username);
    }

    // ---------- Datos generales y habilidades ----------

    private Layout apply(Reactivo reactivo, ReactivoRequestDTO request) {
        Layout layout = resolveLayout(request.idLayout(), reactivo.getLayout());
        List<ReactivoSkill> skills = resolveSkills(request.habilidades(), reactivo);
        List<ReactivoPreviousSkill> previousSkills = resolvePreviousSkills(request.previousSkills());

        reactivo.setTopic(support.requireTopicReference(request.idTema()));
        reactivo.setLayout(layout);
        reactivo.setTitle(request.title().trim());
        reactivo.setInstructions(request.instructions().trim());
        reactivo.setImageUrl(request.coverImageUrl().trim());
        reactivo.setSuggestedTimeMinutes(request.suggestedTimeMinutes());
        reactivo.setStatus(request.status());
        // Reemplazo completo de las dos listas (ver javadoc de Reactivo)
        reactivo.getSkills().clear();
        reactivo.getSkills().addAll(skills);
        reactivo.getPreviousSkills().clear();
        reactivo.getPreviousSkills().addAll(previousSkills);
        return layout;
    }

    /** El layout debe existir y estar activo; uno ya asignado que después se desactivó se puede conservar. */
    private Layout resolveLayout(Long idLayout, Layout current) {
        Layout layout = layoutRepository.findById(idLayout)
                .orElseThrow(() -> new BadRequestException("El layout con ID " + idLayout + " no existe."));
        boolean unchanged = current != null && current.getId().equals(layout.getId());
        if (!Boolean.TRUE.equals(layout.getActive()) && !unchanged) {
            throw new BadRequestException("El layout con ID " + idLayout + " está inactivo.");
        }
        return layout;
    }

    /** Habilidades del catálogo, sin repetir, con peso > 0 y suma exacta de 100. */
    private List<ReactivoSkill> resolveSkills(List<ReactivoSkillRequestDTO> requested, Reactivo reactivo) {
        Set<Long> alreadyAssigned = reactivo.getSkills().stream().map(ReactivoSkill::getSkillId).collect(Collectors.toSet());
        Set<Long> seen = new HashSet<>();
        for (ReactivoSkillRequestDTO dto : requested) {
            if (!seen.add(dto.idHabilidad())) {
                throw new BadRequestException("La habilidad con ID " + dto.idHabilidad() + " está repetida en habilidades.");
            }
        }
        Map<Long, Skill> catalog = skillRepository.findAllById(seen).stream()
                .collect(Collectors.toMap(Skill::getId, Function.identity()));

        BigDecimal total = BigDecimal.ZERO;
        List<ReactivoSkill> skills = new ArrayList<>();
        for (int i = 0; i < requested.size(); i++) {
            ReactivoSkillRequestDTO dto = requested.get(i);
            Skill skill = catalog.get(dto.idHabilidad());
            if (skill == null) {
                throw new BadRequestException("La habilidad con ID " + dto.idHabilidad() + " no existe en el catálogo.");
            }
            if (!Boolean.TRUE.equals(skill.getStatus()) && !alreadyAssigned.contains(skill.getId())) {
                throw new BadRequestException("La habilidad con ID " + dto.idHabilidad() + " está inactiva.");
            }
            total = total.add(dto.peso());
            skills.add(new ReactivoSkill(skill.getId(), dto.peso(), i + 1));
        }
        if (total.compareTo(TOTAL_WEIGHT) != 0) {
            throw new BadRequestException("La suma de los pesos de las habilidades debe ser 100 (actual: "
                    + total.stripTrailingZeros().toPlainString() + ").");
        }
        return skills;
    }

    /** previousSkills llega por nombre (como lo muestra el modal): cada nombre debe existir en el catálogo. */
    private List<ReactivoPreviousSkill> resolvePreviousSkills(List<String> names) {
        if (CurriculumSupport.isEmpty(names)) {
            return List.of();
        }
        Map<String, String> requested = new LinkedHashMap<>();
        for (String name : names) {
            if (name != null && !name.isBlank()) {
                requested.putIfAbsent(name.trim().toLowerCase(Locale.ROOT), name.trim());
            }
        }
        if (requested.isEmpty()) {
            return List.of();
        }
        Map<String, Skill> catalog = new HashMap<>();
        for (Skill skill : skillRepository.findByLowerNames(requested.keySet())) {
            catalog.put(skill.getName().toLowerCase(Locale.ROOT), skill);
        }
        List<ReactivoPreviousSkill> result = new ArrayList<>();
        int order = 1;
        for (Map.Entry<String, String> entry : requested.entrySet()) {
            Skill skill = catalog.get(entry.getKey());
            if (skill == null) {
                throw new BadRequestException("La habilidad previa '" + entry.getValue()
                        + "' no existe en el catálogo de habilidades.");
            }
            result.add(new ReactivoPreviousSkill(skill.getId(), order++));
        }
        return result;
    }

    // ---------- Ejercicios ----------

    /**
     * Valida plantilla y config de cada ejercicio y, en PUT, empareja los que traen "id" con los
     * actuales. Devuelve posición en la lista -> ejercicio existente. No modifica nada.
     */
    private Map<Integer, Exercise> validateExercises(Layout layout, List<ExerciseRequestDTO> exercises,
                                                     Map<Long, Exercise> current) {
        Map<Integer, Exercise> matched = new HashMap<>();
        Set<Long> usedIds = new HashSet<>();
        for (int i = 0; i < exercises.size(); i++) {
            ExerciseRequestDTO dto = exercises.get(i);
            ExerciseRules.checkTemplateType(dto.templateType(), layout, i);
            ExerciseConfigValidator.validate(layout, dto.config(), i);
            String rawId = CurriculumSupport.blankToNull(dto.id());
            if (rawId == null) {
                continue;
            }
            Long exerciseId = ExerciseRules.parseId(rawId, i);
            Exercise existing = current.get(exerciseId);
            if (existing == null) {
                throw new BadRequestException("exercises[" + i + "].id: el ejercicio " + rawId + " no pertenece a este reactivo.");
            }
            if (!usedIds.add(exerciseId)) {
                throw new BadRequestException("exercises[" + i + "].id: el ejercicio " + rawId + " está repetido.");
            }
            matched.put(i, existing);
        }
        return matched;
    }

    private static void applyExercise(Exercise exercise, ExerciseRequestDTO dto) {
        exercise.setSortOrder(dto.order());
        exercise.setInstruction(dto.instruction().trim());
        exercise.setContext(new LinkedHashMap<>(dto.config()));
    }
}
