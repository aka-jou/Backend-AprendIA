package com.aprendia.backend.feature.curriculum.service.impl;

import com.aprendia.backend.exception.BadRequestException;
import com.aprendia.backend.feature.curriculum.entities.Layout;

/** Reglas de ejercicio compartidas por POST/PUT /reactivos y por /exercises. */
final class ExerciseRules {

    private ExerciseRules() {
    }

    /**
     * El ejercicio usa la plantilla del reactivo (el diagrama no le da layout propio). templateType es
     * opcional; si viene, debe coincidir con el código del layout del reactivo.
     */
    static void checkTemplateType(String templateType, Layout layout, Integer position) {
        if (templateType == null || templateType.isBlank() || templateType.trim().equals(layout.getCode())) {
            return;
        }
        String field = position == null ? "templateType" : "exercises[" + position + "].templateType";
        throw new BadRequestException(field + " ('" + templateType.trim() + "') debe ser el layout del reactivo: '"
                + layout.getCode() + "'.");
    }

    /** IDs de ejercicio: se devuelven como texto ("12"); se aceptan como texto o número. */
    static Long parseId(String rawId, int position) {
        try {
            long id = Long.parseLong(rawId.trim());
            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException ignored) {
            // cae al error de abajo
        }
        throw new BadRequestException("exercises[" + position + "].id debe ser el ID numérico de un ejercicio existente.");
    }
}
