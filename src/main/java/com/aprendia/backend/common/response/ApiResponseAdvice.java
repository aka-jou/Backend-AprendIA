package com.aprendia.backend.common.response;

import com.aprendia.backend.common.dto.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Envuelve automáticamente la respuesta de todos los controllers de negocio
 * en el envelope {@link ApiResponse}: { success, data, message }.
 *
 * Gracias a esto los controllers siguen regresando su DTO tal cual
 * (ej. {@code ResponseEntity<RoleResponseDTO>}) y no hay que modificarlos uno por uno.
 *
 * Alcance limitado a {@code com.aprendia.backend.feature} a propósito: así NO se envuelven
 * Swagger/OpenAPI, Actuator ni el /error interno de Spring.
 */
@RestControllerAdvice(basePackages = "com.aprendia.backend.feature")
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        // Un String no puede reescribirse como objeto con el StringHttpMessageConverter
        return !StringHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {

        // Ya viene envuelto (ej. desde GlobalExceptionHandler o un controller que lo armó a mano)
        if (body instanceof ApiResponse<?>) {
            return body;
        }

        // 204 No Content (DELETE): por definición no lleva cuerpo
        if (response instanceof ServletServerHttpResponse servletResponse
                && servletResponse.getServletResponse().getStatus() == HttpStatus.NO_CONTENT.value()) {
            return body;
        }

        ApiMessage apiMessage = returnType.getMethodAnnotation(ApiMessage.class);
        String message = apiMessage != null ? apiMessage.value() : ApiResponse.DEFAULT_SUCCESS_MESSAGE;

        return ApiResponse.ok(body, message);
    }
}
