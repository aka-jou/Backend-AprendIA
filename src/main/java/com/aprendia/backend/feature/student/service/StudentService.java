package com.aprendia.backend.feature.student.service;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.feature.student.dto.StudentRequestDTO;
import com.aprendia.backend.feature.student.dto.StudentResponseDTO;
import org.springframework.data.domain.Pageable;

/** Módulo 4 de la Especificación v2.0: expediente, domicilio y familiares del estudiante. */
public interface StudentService {
    PagedResponse<StudentResponseDTO> getStudents(String query, Pageable pageable);
    StudentResponseDTO getStudentById(Long id);
    StudentResponseDTO createStudent(StudentRequestDTO request);
    StudentResponseDTO updateStudent(Long id, StudentRequestDTO request);
    void deactivateStudent(Long id);
}
