package com.aprendia.backend.feature.curriculum.controllers;

import com.aprendia.backend.common.response.ApiMessage;
import com.aprendia.backend.feature.curriculum.dto.LearningPathTreeDTO;
import com.aprendia.backend.feature.curriculum.service.CurriculumTreeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * La spec publica el árbol en /learning-paths/{id}/tree, fuera del prefijo /catalogs que usa el
 * CRUD de rutas (sección 5.2). Se respeta la ruta tal como la consume el frontend.
 */
@RestController
@RequestMapping("/v1/learning-paths")
@Tag(name = "Árbol curricular", description = "Jerarquía ruta > cuadernillos > unidades > temas en una sola llamada (anexo curricular, sección 6).")
public class LearningPathTreeController {

    @Autowired
    private CurriculumTreeService curriculumTreeService;

    @GetMapping("/{id}/tree")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Árbol curricular obtenido exitosamente.")
    @Operation(summary = "Árbol curricular de una ruta",
            description = "Devuelve idPlanAprendizaje e idRutaAprendizaje con el mismo valor. Solo cuadernillos activos, "
                    + "salvo incluirInactivos=true con rol ADMINISTRADOR o ASESOR_PEDAGOGICO. "
                    + "404 si la ruta no existe (o está inactiva, para quien no gestiona contenido).")
    public ResponseEntity<LearningPathTreeDTO> getTree(
            @PathVariable Long id,
            @Parameter(description = "Incluir cuadernillos con status=false (solo gestores)")
            @RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return ResponseEntity.ok(curriculumTreeService.getTree(id, incluirInactivos));
    }
}
