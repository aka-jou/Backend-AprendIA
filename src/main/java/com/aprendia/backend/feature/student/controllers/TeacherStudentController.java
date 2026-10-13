package com.aprendia.backend.feature.student.controllers;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.common.response.ApiMessage;
import com.aprendia.backend.feature.student.dto.StudentResponseDTO;
import com.aprendia.backend.feature.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Relación docente-estudiante: alumnos asignados a un docente (users.id con rol DOCENTE_AULA). */
@RestController
@RequestMapping("/v1/users/{teacherId}/students")
@Tag(name = "Estudiantes", description = "Expediente, domicilio, familiares y docente asignado de los alumnos (Especificación v2.0, Módulo 4).")
public class TeacherStudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DOCENTE_AULA')")
    @ApiMessage("Estudiantes del docente recuperados exitosamente.")
    @Operation(summary = "Listado paginado de los alumnos de un docente",
            description = "ADMINISTRADOR consulta cualquier docente; DOCENTE_AULA solo su propio id (otro id responde 403). "
                    + "404 si el id no corresponde a un usuario con rol DOCENTE_AULA. "
                    + "Parámetros: page (base 0), size (default 10), query (nombre, correo o CURP exacta).")
    public ResponseEntity<PagedResponse<StudentResponseDTO>> getStudentsByTeacher(
            @PathVariable Long teacherId,
            @Parameter(description = "Texto de búsqueda: nombre, correo o CURP completa")
            @RequestParam(required = false) String query,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(studentService.getStudentsByTeacher(teacherId, query, pageable));
    }
}
