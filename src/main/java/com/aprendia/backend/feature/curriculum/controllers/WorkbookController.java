package com.aprendia.backend.feature.curriculum.controllers;

import com.aprendia.backend.common.dto.ApiResponse;
import com.aprendia.backend.common.response.ApiMessage;
import com.aprendia.backend.feature.curriculum.dto.WorkbookRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.WorkbookResponseDTO;
import com.aprendia.backend.feature.curriculum.service.WorkbookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/workbooks")
@Tag(name = "Cuadernillos", description = "Cuadernillos de trabajo de una ruta de aprendizaje (anexo curricular, sección 2). "
        + "Lectura: cualquier usuario autenticado. Escritura: ADMINISTRADOR o ASESOR_PEDAGOGICO.")
public class WorkbookController {

    private static final String MANAGE = "hasAnyRole('ADMINISTRADOR', 'ASESOR_PEDAGOGICO')";

    @Autowired
    private WorkbookService workbookService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Cuadernillos obtenidos exitosamente.")
    @Operation(summary = "Listado de cuadernillos",
            description = "Lista completa (sin paginar) ordenada por ruta, orden e ID. Excluye los eliminados. "
                    + "Sin permisos de gestión solo devuelve cuadernillos con status=true. "
                    + "404 si idRutaAprendizaje no existe.")
    public ResponseEntity<List<WorkbookResponseDTO>> getAll(
            @Parameter(description = "Filtra por ruta de aprendizaje") @RequestParam(required = false) Long idRutaAprendizaje,
            @Parameter(description = "Texto a buscar en nombre, autor o descripción (sin distinguir mayúsculas)") @RequestParam(required = false) String query,
            @Parameter(description = "Solo gestores: true activos, false borradores; se ignora para el resto") @RequestParam(required = false) Boolean status) {
        return ResponseEntity.ok(workbookService.getWorkbooks(idRutaAprendizaje, query, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Cuadernillo obtenido exitosamente.")
    @Operation(summary = "Detalle de un cuadernillo")
    public ResponseEntity<WorkbookResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(workbookService.getWorkbookById(id));
    }

    @PostMapping
    @PreAuthorize(MANAGE)
    @ApiMessage("Cuadernillo registrado exitosamente.")
    @Operation(summary = "Registrar cuadernillo", description = "orden es opcional (por defecto 1). 400 si la ruta no existe.")
    public ResponseEntity<WorkbookResponseDTO> create(@Valid @RequestBody WorkbookRequestDTO request) {
        return new ResponseEntity<>(workbookService.createWorkbook(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize(MANAGE)
    @ApiMessage("Cuadernillo actualizado exitosamente.")
    @Operation(summary = "Actualizar cuadernillo",
            description = "Reemplaza los datos del cuadernillo. status=false lo pasa a inactivo/borrador (desactivar). "
                    + "Si se omite orden, se conserva el actual.")
    public ResponseEntity<WorkbookResponseDTO> update(@PathVariable Long id, @Valid @RequestBody WorkbookRequestDTO request) {
        return ResponseEntity.ok(workbookService.updateWorkbook(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(MANAGE)
    @Operation(summary = "Eliminar cuadernillo",
            description = "Baja lógica: el cuadernillo y sus unidades, temas y recursos dejan de existir para la API "
                    + "(listados y GET responden sin ellos / 404), pero se conservan en la base. Para solo ocultarlo use PUT con status=false.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        workbookService.deleteWorkbook(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Cuadernillo eliminado exitosamente."));
    }
}
