package com.aprendia.backend.feature.student.controllers;

import com.aprendia.backend.common.dto.ApiResponse;
import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.common.response.ApiMessage;
import com.aprendia.backend.feature.student.dto.StudentRequestDTO;
import com.aprendia.backend.feature.student.dto.StudentResponseDTO;
import com.aprendia.backend.feature.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/students")
@Tag(name = "Estudiantes", description = "Expediente, domicilio y familiares de los alumnos (Especificación v2.0, Módulo 4). Requieren JWT y rol ADMINISTRADOR.")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Estudiantes recuperados exitosamente.")
    @Operation(summary = "Listado paginado de estudiantes",
            description = "Parámetros: page (base 0), size (default 10), query (busca en nombre, correo o CURP exacta).")
    public ResponseEntity<PagedResponse<StudentResponseDTO>> getStudents(
            @Parameter(description = "Texto de búsqueda: nombre, correo o CURP completa")
            @RequestParam(required = false) String query,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(studentService.getStudents(query, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Estudiante registrado exitosamente.")
    @Operation(summary = "Crear expediente completo del alumno", description = "Persona, domicilio, familiares, perfil pedagógico y correo de acceso a la app móvil.")
    public ResponseEntity<StudentResponseDTO> createStudent(@Valid @RequestBody StudentRequestDTO request) {
        return new ResponseEntity<>(studentService.createStudent(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Expediente del estudiante obtenido exitosamente.")
    @Operation(summary = "Expediente clínico/pedagógico completo del estudiante")
    public ResponseEntity<StudentResponseDTO> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Estudiante actualizado exitosamente.")
    @Operation(summary = "Actualizar datos del estudiante o familiares", description = "Reemplazo completo: los familiares que no se envíen se eliminan.")
    public ResponseEntity<StudentResponseDTO> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequestDTO request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desactivar alumno", description = "Baja lógica de la cuenta del alumno; el expediente se conserva.")
    public ResponseEntity<ApiResponse<Void>> deactivateStudent(@PathVariable Long id) {
        studentService.deactivateStudent(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Estudiante desactivado exitosamente."));
    }
}
