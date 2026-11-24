package com.aprendia.backend.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Escribe eventos en el log de auditoría (logs/audit/audit-AAAA-MM-DD.log, logger AUDIT).
 *
 * Lo usa {@link AuditAspect} para los endpoints y puede inyectarse en cualquier servicio que necesite
 * registrar una acción fuera de una petición HTTP (tareas programadas, procesos internos).
 * Nunca lanza excepciones: un fallo al auditar no interrumpe la operación auditada.
 */
@Component
public class AuditLogger {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");
    private static final Logger LOG = LoggerFactory.getLogger(AuditLogger.class);

    /**
     * @param action       acción realizada (CREATE, UPDATE, STATUS_CHANGE, DELETE, LOGIN_OTP_VERIFY...)
     * @param eventType    tipo ECS (creation, change, deletion, start, info)
     * @param category     categoría ECS (authentication, iam, database)
     * @param resourceType recurso afectado (users, students, catalogs.roles...)
     * @param resourceId   identificador del recurso, si aplica
     * @param actor        usuario que ejecuta la acción ("anonymous" si no hay sesión)
     * @param success      resultado de la operación
     * @param reason       causa del fallo (tipo y mensaje de la excepción); null si tuvo éxito
     * @param extra        campos adicionales (método HTTP, ruta, parámetros de ruta, nuevo estado)
     */
    public void record(String action, String eventType, String category, String resourceType, String resourceId,
                       String actor, boolean success, String reason, Map<String, ?> extra) {
        try {
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put(LogFields.EVENT_DATASET, LogFields.DATASET_AUDIT);
            fields.put(LogFields.EVENT_ACTION, action);
            fields.put(LogFields.EVENT_CATEGORY, category);
            fields.put(LogFields.EVENT_TYPE, eventType);
            fields.put(LogFields.EVENT_OUTCOME, success ? "success" : "failure");
            fields.put(LogFields.EVENT_REASON, reason);
            fields.put(LogFields.RESOURCE_TYPE, resourceType);
            fields.put(LogFields.RESOURCE_ID, resourceId);
            fields.put(LogFields.USER_NAME, actor);
            if (extra != null) {
                fields.putAll(extra);
            }

            StringBuilder message = new StringBuilder()
                    .append(action).append(' ').append(resourceType);
            if (resourceId != null) {
                message.append(" id=").append(resourceId);
            }
            message.append(" por ").append(actor).append(": ").append(success ? "éxito" : "fallo");
            if (reason != null) {
                message.append(" (").append(reason).append(')');
            }

            LogFields.log(AUDIT, success ? Level.INFO : Level.WARN, fields, message.toString());
        } catch (RuntimeException ex) {
            LOG.warn("No se pudo escribir el evento de auditoría {} {}: {}", action, resourceType, ex.getMessage());
        }
    }
}
