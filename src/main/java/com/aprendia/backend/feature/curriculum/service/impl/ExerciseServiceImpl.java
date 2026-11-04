package com.aprendia.backend.feature.curriculum.service.impl;

import com.aprendia.backend.common.security.CurrentUser;
import com.aprendia.backend.exception.ResourceNotFoundException;
import com.aprendia.backend.feature.curriculum.dto.ExerciseRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.ExerciseResponseDTO;
import com.aprendia.backend.feature.curriculum.entities.Exercise;
import com.aprendia.backend.feature.curriculum.entities.Reactivo;
import com.aprendia.backend.feature.curriculum.repository.ExerciseRepository;
import com.aprendia.backend.feature.curriculum.service.CurriculumSupport;
import com.aprendia.backend.feature.curriculum.service.ExerciseConfigValidator;
import com.aprendia.backend.feature.curriculum.service.ExerciseService;
import com.aprendia.backend.feature.curriculum.service.ReactivoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

/** CRUD directo de ejercicios individuales de un reactivo (spec, sección 5.2). */
@Service
public class ExerciseServiceImpl implements ExerciseService {

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private CurriculumSupport support;

    @Autowired
    private ReactivoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseResponseDTO> getExercises(Long idReactivo) {
        Reactivo reactivo = support.findVisibleReactivo(idReactivo);
        return exerciseRepository.findByReactivoIds(List.of(idReactivo)).stream()
                .map(exercise -> mapper.toExerciseResponse(exercise, reactivo.getLayout()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseResponseDTO getExercise(Long id) {
        Exercise exercise = findVisible(id);
        return mapper.toExerciseResponse(exercise, exercise.getReactivo().getLayout());
    }

    @Override
    @Transactional
    public ExerciseResponseDTO createExercise(Long idReactivo, ExerciseRequestDTO request) {
        Reactivo reactivo = support.findVisibleReactivo(idReactivo);
        validate(reactivo, request);
        Exercise exercise = new Exercise();
        exercise.setReactivo(reactivo);
        apply(exercise, request);
        exercise.markCreated(CurrentUser.username(), LocalDateTime.now());
        return mapper.toExerciseResponse(exerciseRepository.save(exercise), reactivo.getLayout());
    }

    /** No cambia de reactivo: el "id" del body, si viene, se ignora. */
    @Override
    @Transactional
    public ExerciseResponseDTO updateExercise(Long id, ExerciseRequestDTO request) {
        Exercise exercise = findVisible(id);
        Reactivo reactivo = exercise.getReactivo();
        validate(reactivo, request);
        apply(exercise, request);
        exercise.markUpdated(CurrentUser.username(), LocalDateTime.now());
        return mapper.toExerciseResponse(exerciseRepository.save(exercise), reactivo.getLayout());
    }

    @Override
    @Transactional
    public void deleteExercise(Long id) {
        Exercise exercise = findVisible(id);
        exercise.markDeleted(CurrentUser.username(), LocalDateTime.now());
        exerciseRepository.save(exercise);
    }

    /** El ejercicio existe si existe él y es visible su reactivo; si no, 404 del ejercicio. */
    private Exercise findVisible(Long id) {
        Exercise exercise = exerciseRepository.findActiveById(id).orElseThrow(() -> notFound(id));
        try {
            exercise.setReactivo(support.findVisibleReactivo(exercise.getReactivo().getId()));
        } catch (ResourceNotFoundException e) {
            throw notFound(id);
        }
        return exercise;
    }

    private static void validate(Reactivo reactivo, ExerciseRequestDTO request) {
        ExerciseRules.checkTemplateType(request.templateType(), reactivo.getLayout(), null);
        ExerciseConfigValidator.validate(reactivo.getLayout(), request.config(), null);
    }

    private static void apply(Exercise exercise, ExerciseRequestDTO request) {
        exercise.setSortOrder(request.order());
        exercise.setInstruction(request.instruction().trim());
        exercise.setContext(new LinkedHashMap<>(request.config()));
    }

    private static ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Ejercicio no encontrado con ID: " + id);
    }
}
