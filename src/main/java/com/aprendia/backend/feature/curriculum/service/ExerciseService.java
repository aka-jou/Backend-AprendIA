package com.aprendia.backend.feature.curriculum.service;

import com.aprendia.backend.feature.curriculum.dto.ExerciseRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.ExerciseResponseDTO;

import java.util.List;

public interface ExerciseService {
    List<ExerciseResponseDTO> getExercises(Long idReactivo);
    ExerciseResponseDTO getExercise(Long id);
    ExerciseResponseDTO createExercise(Long idReactivo, ExerciseRequestDTO request);
    ExerciseResponseDTO updateExercise(Long id, ExerciseRequestDTO request);
    void deleteExercise(Long id);
}
