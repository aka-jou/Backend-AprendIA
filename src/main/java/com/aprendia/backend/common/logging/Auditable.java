package com.aprendia.backend.common.logging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Personaliza el registro de auditoría de un endpoint.
 *
 * No es obligatoria: {@link AuditAspect} audita automáticamente todo endpoint POST, PUT, PATCH y DELETE
 * de los controladores, derivando la acción del verbo HTTP y el recurso de la ruta. Esta anotación
 * solo se usa cuando hace falta un nombre de acción explícito o identificar al actor de una petición
 * anónima (por ejemplo, el correo con el que se intenta iniciar sesión).
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /** Nombre de la acción, p. ej. LOGIN_OTP_VERIFY. Vacío: CREATE, UPDATE, STATUS_CHANGE o DELETE según el verbo. */
    String action() default "";

    /** Tipo de recurso, p. ej. auth. Vacío: se deriva de la ruta del controlador (users, students.relatives...). */
    String resource() default "";

    /** Categoría ECS (authentication, iam, database...). Vacío: se deriva del recurso. */
    String category() default "";

    /**
     * Expresión SpEL sobre los parámetros del método que identifica al actor cuando la petición
     * no está autenticada, p. ej. "#request.email()". Nunca debe apuntar a contraseñas ni códigos.
     */
    String actor() default "";
}
