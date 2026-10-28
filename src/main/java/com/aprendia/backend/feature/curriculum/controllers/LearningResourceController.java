package com.aprendia.backend.feature.curriculum.controllers;

import com.aprendia.backend.common.dto.ApiResponse;
import com.aprendia.backend.common.response.ApiMessage;
import com.aprendia.backend.feature.curriculum.dto.ResourceRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.ResourceResponseDTO;
import com.aprendia.backend.feature.curriculum.service.LearningResourceService;
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
@RequestMapping("/v1/resources")
@Tag(name = "Recursos didácticos", description = "Recursos (diapositivas) de un tema (anexo curricular, sección 5). "
        + "GET por id, PUT y DELETE no están en la spec; se agregan por consistencia con /workbooks.")
public class LearningResourceController {

    private static final String MANAGE = "hasAnyRole('ADMINISTRADOR', 'ASESOR_PEDAGOGICO')";

    @Autowired
    private LearningResourceService learningResourceService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Recursos didácticos obtenidos exitosamente.")
    @Operation(summary = "Recursos de un tema", description = "idTema es obligatorio (400 si falta). Incluye el contenido completo de cada recurso.")
    public ResponseEntity<List<ResourceResponseDTO>> getByTopic(
            @Parameter(description = "Tema del que se listan los recursos", required = true) @RequestParam Long idTema) {
        return ResponseEntity.ok(learningResourceService.getResourcesByTopic(idTema));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Recurso didáctico obtenido exitosamente.")
    @Operation(summary = "Detalle de un recurso")
    public ResponseEntity<ResourceResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(learningResourceService.getResourceById(id));
    }

    @PostMapping
    @PreAuthorize(MANAGE)
    @ApiMessage("Recurso didáctico registrado exitosamente.")
    @Operation(summary = "Registrar recurso", description = "idLayout es opcional y se guarda sin validar contra un catálogo. "
            + "contenido.slides requiere al menos una diapositiva con order y title.")
    public ResponseEntity<ResourceResponseDTO> create(@Valid @RequestBody ResourceRequestDTO request) {
        return new ResponseEntity<>(learningResourceService.createResource(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize(MANAGE)
    @ApiMessage("Recurso didáctico actualizado exitosamente.")
    @Operation(summary = "Actualizar recurso", description = "Reemplaza datos y contenido completo del recurso.")
    public ResponseEntity<ResourceResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ResourceRequestDTO request) {
        return ResponseEntity.ok(learningResourceService.updateResource(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(MANAGE)
    @Operation(summary = "Eliminar recurso", description = "Baja lógica.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        learningResourceService.deleteResource(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Recurso didáctico eliminado exitosamente."));
    }
}
