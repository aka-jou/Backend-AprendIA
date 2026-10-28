package com.aprendia.backend.feature.curriculum.dto;

import lombok.Builder;

import java.util.List;

/**
 * Respuesta de GET /learning-paths/{id}/tree (anexo curricular, sección 6).
 *
 * La spec nombra el identificador de la ruta "idPlanAprendizaje" solo en este endpoint; el resto del
 * documento usa "idRutaAprendizaje". Se devuelven ambos con el mismo valor para no romper al frontend
 * que se haya construido con cualquiera de los dos.
 */
@Builder
public record LearningPathTreeDTO(
    Long idPlanAprendizaje,
    Long idRutaAprendizaje,
    String nombre,
    String metodologia,
    String perfil,
    String urlImagen,
    List<WorkbookNode> cuadernillos
) {

    @Builder
    public record WorkbookNode(
        Long idCuadernillo,
        String nombre,
        String descripcion,
        String objetivo,
        String autor,
        String urlImagen,
        Boolean status,
        Integer orden,
        List<UnitNode> unidades
    ) {}

    @Builder
    public record UnitNode(
        Long idUnidad,
        String nombre,
        String urlImagen,
        Integer orden,
        List<TopicNode> temas
    ) {}

    @Builder
    public record TopicNode(
        Long idTema,
        String nombre,
        String descripcion,
        String urlImagen,
        Integer orden,
        Long totalRecursos,
        Long totalReactivos
    ) {}
}
