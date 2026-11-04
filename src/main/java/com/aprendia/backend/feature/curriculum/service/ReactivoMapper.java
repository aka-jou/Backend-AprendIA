package com.aprendia.backend.feature.curriculum.service;

import com.aprendia.backend.common.util.DateFormats;
import com.aprendia.backend.feature.catalogs.entities.Skill;
import com.aprendia.backend.feature.catalogs.repository.SkillRepository;
import com.aprendia.backend.feature.curriculum.dto.ExerciseResponseDTO;
import com.aprendia.backend.feature.curriculum.dto.ReactivoResponseDTO;
import com.aprendia.backend.feature.curriculum.dto.ReactivoResponseDTO.LayoutRef;
import com.aprendia.backend.feature.curriculum.dto.ReactivoResponseDTO.MobileInstance;
import com.aprendia.backend.feature.curriculum.dto.ReactivoResponseDTO.MobileReactivo;
import com.aprendia.backend.feature.curriculum.dto.ReactivoResponseDTO.MobileSkillWeight;
import com.aprendia.backend.feature.curriculum.dto.ReactivoResponseDTO.SkillWeight;
import com.aprendia.backend.feature.curriculum.dto.ReactivoResponseDTO.WebExercise;
import com.aprendia.backend.feature.curriculum.entities.Exercise;
import com.aprendia.backend.feature.curriculum.entities.Layout;
import com.aprendia.backend.feature.curriculum.entities.Reactivo;
import com.aprendia.backend.feature.curriculum.entities.ReactivoPreviousSkill;
import com.aprendia.backend.feature.curriculum.entities.ReactivoSkill;
import com.aprendia.backend.feature.curriculum.repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Arma la respuesta de reactivos y ejercicios. Para una lista de reactivos usa un número fijo de
 * consultas (ejercicios y nombres de habilidades), sin importar cuántos reactivos sean.
 */
@Component
public class ReactivoMapper {

    /** Versión del contrato que devuelve cada reactivo (campo "version" de la spec). */
    public static final String CONTRACT_VERSION = "2.0.0";

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private SkillRepository skillRepository;

    public ReactivoResponseDTO toResponse(Reactivo reactivo) {
        return toResponses(List.of(reactivo)).get(0);
    }

    public List<ReactivoResponseDTO> toResponses(List<Reactivo> reactivos) {
        if (reactivos.isEmpty()) {
            return List.of();
        }
        Map<Long, List<Exercise>> exercisesByReactivo = exerciseRepository
                .findByReactivoIds(reactivos.stream().map(Reactivo::getId).toList()).stream()
                .collect(Collectors.groupingBy(exercise -> exercise.getReactivo().getId()));

        Set<Long> skillIds = new HashSet<>();
        for (Reactivo reactivo : reactivos) {
            reactivo.getSkills().forEach(skill -> skillIds.add(skill.getSkillId()));
            reactivo.getPreviousSkills().forEach(skill -> skillIds.add(skill.getSkillId()));
        }
        Map<Long, String> skillNames = skillRepository.findAllById(skillIds).stream()
                .collect(Collectors.toMap(Skill::getId, Skill::getName, (a, b) -> a));

        return reactivos.stream()
                .map(reactivo -> build(reactivo, exercisesByReactivo.getOrDefault(reactivo.getId(), List.of()), skillNames))
                .toList();
    }

    public ExerciseResponseDTO toExerciseResponse(Exercise exercise, Layout layout) {
        String id = String.valueOf(exercise.getId());
        String reactivoId = String.valueOf(exercise.getReactivo().getId());
        return ExerciseResponseDTO.builder()
                .id(id)
                .idEjercicio(id)
                .reactivoId(reactivoId)
                .idReactivo(reactivoId)
                .order(exercise.getSortOrder())
                .orden(exercise.getSortOrder())
                .templateType(layout.getCode())
                .instruction(exercise.getInstruction())
                .instruccion(exercise.getInstruction())
                .config(exercise.getContext())
                .contexto(exercise.getContext())
                .build();
    }

    private ReactivoResponseDTO build(Reactivo reactivo, List<Exercise> exercises, Map<Long, String> skillNames) {
        String id = String.valueOf(reactivo.getId());
        Layout layout = reactivo.getLayout();
        Function<Long, String> nameOf = skillId -> skillNames.get(skillId);

        List<ReactivoSkill> skills = reactivo.getSkills();
        List<SkillWeight> habilidades = skills.stream()
                .map(skill -> new SkillWeight(id, skill.getSkillId(), nameOf.apply(skill.getSkillId()), plain(skill.getWeight())))
                .toList();
        List<MobileSkillWeight> mobileHabilidades = skills.stream()
                .map(skill -> new MobileSkillWeight(skill.getSkillId(), nameOf.apply(skill.getSkillId()), plain(skill.getWeight())))
                .toList();
        // targetSkills son las habilidades evaluadas; previousSkills, las requeridas (reactivo_previous_skills)
        List<String> targetSkills = skills.stream().map(skill -> nameOf.apply(skill.getSkillId())).toList();
        List<String> previousSkills = reactivo.getPreviousSkills().stream()
                .map(ReactivoPreviousSkill::getSkillId)
                .map(nameOf)
                .toList();

        List<WebExercise> webExercises = exercises.stream()
                .map(exercise -> new WebExercise(String.valueOf(exercise.getId()), id, exercise.getSortOrder(),
                        layout.getCode(), exercise.getInstruction(), exercise.getContext()))
                .toList();
        List<MobileInstance> instances = exercises.stream()
                .map(exercise -> new MobileInstance(String.valueOf(exercise.getId()), exercise.getSortOrder(),
                        layout.getCode(), exercise.getInstruction(), exercise.getContext()))
                .toList();

        return ReactivoResponseDTO.builder()
                .version(CONTRACT_VERSION)
                .idReactivo(id)
                .id(id)
                .idTema(reactivo.getTopic().getId())
                .topicId("tema-" + reactivo.getTopic().getId())
                .topicName(reactivo.getTopic().getName())
                .idLayout(layout.getId())
                .layoutCode(layout.getCode())
                .layoutName(layout.getName())
                .title(reactivo.getTitle())
                .instructions(reactivo.getInstructions())
                .coverImageUrl(reactivo.getImageUrl())
                .suggestedTimeMinutes(reactivo.getSuggestedTimeMinutes())
                .status(reactivo.getStatus())
                .habilidades(habilidades)
                .targetSkills(targetSkills)
                .previousSkills(previousSkills)
                .layout(new LayoutRef(layout.getId(), layout.getCode(), layout.getName()))
                .reactivo(MobileReactivo.builder()
                        .titulo(reactivo.getTitle())
                        .instrucciones(reactivo.getInstructions())
                        .coverImageUrl(reactivo.getImageUrl())
                        .tiempoSugeridoMinutos(reactivo.getSuggestedTimeMinutes())
                        .habilidades(mobileHabilidades)
                        .targetSkills(targetSkills)
                        .previousSkills(previousSkills)
                        .build())
                .instances(instances)
                .exercises(webExercises)
                .createdAt(DateFormats.isoInstant(reactivo.getCreatedAt()))
                .updatedAt(DateFormats.isoInstant(reactivo.getUpdatedAt()))
                .build();
    }

    /** 34.00 -> 34 y 33.50 -> 33.5, sin notación científica (100 no sale como 1E+2). */
    static BigDecimal plain(BigDecimal value) {
        if (value == null) {
            return null;
        }
        BigDecimal stripped = value.stripTrailingZeros();
        return stripped.scale() < 0 ? stripped.setScale(0) : stripped;
    }
}
