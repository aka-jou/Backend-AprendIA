package com.aprendia.backend.feature.curriculum.controllers;

import com.aprendia.backend.common.dto.ApiResponse;
import com.aprendia.backend.common.response.ApiMessage;
import com.aprendia.backend.feature.curriculum.dto.TopicRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.TopicResponseDTO;
import com.aprendia.backend.feature.curriculum.service.TopicService;
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
@RequestMapping("/v1/topics")
@Tag(name = "Temas curriculares", description = "Temas de una unidad (anexo curricular, sección 4). "
        + "GET por id, PUT y DELETE no están en la spec; se agregan por consistencia con /workbooks. "
        + "totalReactivos vale 0 hasta que exista el módulo de reactivos.")
public class TopicController {

    private static final String MANAGE = "hasAnyRole('ADMINISTRADOR', 'ASESOR_PEDAGOGICO')";

    @Autowired
    private TopicService topicService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Temas obtenidos exitosamente.")
    @Operation(summary = "Listado de temas", description = "Ordenado por orden e ID. 404 si idUnidad no existe o no es visible.")
    public ResponseEntity<List<TopicResponseDTO>> getAll(
            @Parameter(description = "Filtra por unidad") @RequestParam(required = false) Long idUnidad) {
        return ResponseEntity.ok(topicService.getTopics(idUnidad));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Tema obtenido exitosamente.")
    @Operation(summary = "Detalle de un tema")
    public ResponseEntity<TopicResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(topicService.getTopicById(id));
    }

    @PostMapping
    @PreAuthorize(MANAGE)
    @ApiMessage("Tema registrado exitosamente.")
    @Operation(summary = "Registrar tema", description = "orden es opcional (por defecto 1). 400 si la unidad no existe.")
    public ResponseEntity<TopicResponseDTO> create(@Valid @RequestBody TopicRequestDTO request) {
        return new ResponseEntity<>(topicService.createTopic(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize(MANAGE)
    @ApiMessage("Tema actualizado exitosamente.")
    @Operation(summary = "Actualizar tema", description = "Permite moverlo a otra unidad cambiando idUnidad. Si se omite orden, se conserva.")
    public ResponseEntity<TopicResponseDTO> update(@PathVariable Long id, @Valid @RequestBody TopicRequestDTO request) {
        return ResponseEntity.ok(topicService.updateTopic(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(MANAGE)
    @Operation(summary = "Eliminar tema", description = "Baja lógica del tema y de sus recursos.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        topicService.deleteTopic(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Tema eliminado exitosamente."));
    }
}
