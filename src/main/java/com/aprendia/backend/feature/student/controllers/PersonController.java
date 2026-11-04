package com.aprendia.backend.feature.student.controllers;

import com.aprendia.backend.common.response.ApiMessage;
import com.aprendia.backend.feature.student.dto.PersonLookupDTO;
import com.aprendia.backend.feature.student.service.StudentRelativeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/persons")
@Tag(name = "Personas", description = "Búsqueda de persona por CURP para vincular familiares sin duplicarlos.")
public class PersonController {

    @Autowired
    private StudentRelativeService relativeService;

    @GetMapping("/by-curp/{curp}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ASESOR_PEDAGOGICO', 'DOCENTE_AULA')")
    @ApiMessage("Persona encontrada exitosamente.")
    @Operation(summary = "Buscar persona por CURP", description = "CURP exacta de 18 caracteres, sin distinguir mayúsculas. "
            + "Si existe, el frontend envía su personId en relatives[] para vincularla. 404 si no existe; 400 si el formato es inválido.")
    public ResponseEntity<PersonLookupDTO> getByCurp(@PathVariable String curp) {
        return ResponseEntity.ok(relativeService.findPersonByCurp(curp));
    }
}
