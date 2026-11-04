package com.aprendia.backend.feature.student.controllers;

import com.aprendia.backend.common.dto.ApiResponse;
import com.aprendia.backend.common.response.ApiMessage;
import com.aprendia.backend.feature.student.dto.StudentRelativeDTO;
import com.aprendia.backend.feature.student.service.StudentRelativeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/students/{studentId}/relatives")
@Tag(name = "Familiares del estudiante", description = "CRUD independiente de familiares (spec de búsqueda por CURP, sección 4). "
        + "Lectura: ADMINISTRADOR, ASESOR_PEDAGOGICO y DOCENTE_AULA (solo sus alumnos). Escritura: ADMINISTRADOR y ASESOR_PEDAGOGICO.")
public class StudentRelativeController {

    private static final String MANAGE = "hasAnyRole('ADMINISTRADOR', 'ASESOR_PEDAGOGICO')";

    @Autowired
    private StudentRelativeService relativeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ASESOR_PEDAGOGICO', 'DOCENTE_AULA')")
    @ApiMessage("Familiares obtenidos exitosamente.")
    @Operation(summary = "Listar familiares vinculados al estudiante")
    public ResponseEntity<List<StudentRelativeDTO>> getAll(@PathVariable Long studentId) {
        return ResponseEntity.ok(relativeService.getRelatives(studentId));
    }

    @PostMapping
    @PreAuthorize(MANAGE)
    @ApiMessage("Familiar vinculado exitosamente.")
    @Operation(summary = "Agregar o vincular un familiar", description = "Con personId vincula esa persona; sin personId "
            + "vincula la persona de la misma CURP o la crea. 409 si ya está vinculado al alumno.")
    public ResponseEntity<StudentRelativeDTO> create(@PathVariable Long studentId, @Valid @RequestBody StudentRelativeDTO request) {
        return new ResponseEntity<>(relativeService.addRelative(studentId, request), HttpStatus.CREATED);
    }

    @PutMapping("/{relativeId}")
    @PreAuthorize(MANAGE)
    @ApiMessage("Familiar actualizado exitosamente.")
    @Operation(summary = "Actualizar parentesco o datos del familiar", description = "Reemplazo completo de los datos de la "
            + "persona (firstName, lastName y relationship obligatorios). Se reflejan en todos los alumnos vinculados a ella. "
            + "Si la persona es también estudiante o usuario del sistema, solo se actualiza el parentesco.")
    public ResponseEntity<StudentRelativeDTO> update(@PathVariable Long studentId, @PathVariable Long relativeId,
                                                     @Valid @RequestBody StudentRelativeDTO request) {
        return ResponseEntity.ok(relativeService.updateRelative(studentId, relativeId, request));
    }

    @DeleteMapping("/{relativeId}")
    @PreAuthorize(MANAGE)
    @Operation(summary = "Desvincular familiar", description = "Elimina solo el vínculo; la persona se conserva.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long studentId, @PathVariable Long relativeId) {
        relativeService.removeRelative(studentId, relativeId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Familiar desvinculado exitosamente."));
    }
}
