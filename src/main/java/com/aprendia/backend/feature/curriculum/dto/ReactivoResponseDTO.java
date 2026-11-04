package com.aprendia.backend.feature.curriculum.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Reactivo con el formato del frontend web y, a la vez, el que pide la app móvil
 * (CONTRATO_MOVIL_EJERCICIOS_Y_FLUJO): layout, reactivo e instances son espejos de los campos web.
 * Los IDs se devuelven como texto porque así los tipa el contrato ("id: string").
 */
@Builder
public record ReactivoResponseDTO(
    String version,
    String idReactivo,
    String id,
    Long idTema,
    String topicId,
    String topicName,
    Long idLayout,
    String layoutCode,
    String layoutName,
    String title,
    String instructions,
    String coverImageUrl,
    Integer suggestedTimeMinutes,
    String status,
    List<SkillWeight> habilidades,
    List<String> targetSkills,
    List<String> previousSkills,
    LayoutRef layout,
    MobileReactivo reactivo,
    List<MobileInstance> instances,
    List<WebExercise> exercises,
    String createdAt,
    String updatedAt
) {

    @Builder
    public record SkillWeight(String idReactivo, Long idHabilidad, String nombreHabilidad, BigDecimal peso) {
    }

    @Builder
    public record MobileSkillWeight(Long idHabilidad, String nombreHabilidad, BigDecimal peso) {
    }

    @Builder
    public record LayoutRef(Long idLayout, String codigo, String nombre) {
    }

    @Builder
    public record MobileReactivo(
        String titulo,
        String instrucciones,
        String coverImageUrl,
        Integer tiempoSugeridoMinutos,
        List<MobileSkillWeight> habilidades,
        List<String> targetSkills,
        List<String> previousSkills
    ) {
    }

    @Builder
    public record MobileInstance(String idEjercicio, Integer orden, String templateType, String instruccion,
                                 Map<String, Object> contexto) {
    }

    @Builder
    public record WebExercise(String id, String reactivoId, Integer order, String templateType, String instruction,
                              Map<String, Object> config) {
    }
}
