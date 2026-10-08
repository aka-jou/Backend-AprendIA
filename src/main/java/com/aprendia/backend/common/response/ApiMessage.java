package com.aprendia.backend.common.response;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define el {@code message} del envelope para un endpoint concreto.
 *
 * <pre>
 * &#64;GetMapping
 * &#64;ApiMessage("Metodologías obtenidas exitosamente.")
 * public ResponseEntity&lt;PagedResponse&lt;MetodologyResponseDTO&gt;&gt; getAll(...) { ... }
 * </pre>
 *
 * Si el método no la tiene, se usa {@code ApiResponse.DEFAULT_SUCCESS_MESSAGE}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiMessage {
    String value();
}
