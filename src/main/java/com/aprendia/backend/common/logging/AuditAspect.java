package com.aprendia.backend.common.logging;

import com.aprendia.backend.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Auditoría automática de todas las acciones de escritura de la API.
 *
 * Intercepta cada método de un @RestController anotado con @PostMapping, @PutMapping, @PatchMapping,
 * @DeleteMapping o {@link Auditable}, y registra en el log de auditoría:
 *   - acción (CREATE, UPDATE, STATUS_CHANGE, DELETE o la definida en @Auditable),
 *   - recurso derivado de la ruta (p. ej. /v1/students/{studentId}/relatives -> students.relatives),
 *   - identificador del recurso (variable de ruta o, en altas, el id devuelto en la respuesta),
 *   - actor (usuario del JWT, o el definido por @Auditable.actor en peticiones anónimas como el login),
 *   - resultado (success / failure) y, si falla, el tipo y mensaje de la excepción.
 *
 * Orden HIGHEST_PRECEDENCE: envuelve también a @PreAuthorize, por lo que los intentos sin permisos
 * quedan auditados como failure (AccessDeniedException).
 *
 * No se registran cuerpos de petición: así nunca llegan al log contraseñas, códigos OTP ni datos cifrados.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuditAspect {

    private static final String ANONYMOUS = "anonymous";

    private final AuditLogger auditLogger;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public AuditAspect(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    @Around("within(com.aprendia.backend..*) "
            + "&& @within(org.springframework.web.bind.annotation.RestController) "
            + "&& (@annotation(org.springframework.web.bind.annotation.PostMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.PutMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.PatchMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping) "
            + "|| @annotation(com.aprendia.backend.common.logging.Auditable))")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> controllerClass = joinPoint.getTarget() != null
                ? AopUtils.getTargetClass(joinPoint.getTarget())
                : method.getDeclaringClass();
        Auditable auditable = AnnotatedElementUtils.findMergedAnnotation(method, Auditable.class);
        HttpServletRequest request = currentRequest();
        Object[] args = joinPoint.getArgs();

        String httpMethod = request != null ? request.getMethod() : declaredHttpMethod(method);
        String methodPath = firstPath(AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class));
        String classPath = firstPath(AnnotatedElementUtils.findMergedAnnotation(controllerClass, RequestMapping.class));

        String action = auditable != null && StringUtils.hasText(auditable.action())
                ? auditable.action()
                : deriveAction(httpMethod, methodPath);
        String resource = auditable != null && StringUtils.hasText(auditable.resource())
                ? auditable.resource()
                : deriveResource(classPath, methodPath);
        String category = auditable != null && StringUtils.hasText(auditable.category())
                ? auditable.category()
                : deriveCategory(resource);
        String actor = resolveActor(auditable, method, args);
        Map<String, String> pathVariables = pathVariables(request);

        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put(LogFields.HTTP_METHOD, httpMethod);
        if (request != null) {
            extra.put(LogFields.URL_PATH, request.getRequestURI());
        }
        if (!pathVariables.isEmpty()) {
            extra.put(LogFields.PATH_PARAMS, pathVariables.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining(", ")));
        }
        if ("STATUS_CHANGE".equals(action)) {
            extra.put(LogFields.NEW_STATUS, findNewStatus(args));
        }

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            String reason = ex.getMessage() == null
                    ? ex.getClass().getSimpleName()
                    : ex.getClass().getSimpleName() + ": " + LogFields.truncate(ex.getMessage(), 300);
            auditLogger.record(action, eventType(action), category, resource,
                    resourceIdFromPath(pathVariables), actor, false, reason, extra);
            throw ex;
        }

        String resourceId = "CREATE".equals(action)
                ? firstNonNull(extractId(result), resourceIdFromPath(pathVariables))
                : firstNonNull(resourceIdFromPath(pathVariables), extractId(result));
        auditLogger.record(action, eventType(action), category, resource, resourceId, actor, true, null, extra);
        return result;
    }

    // ---------------------------------------------------------------- derivación de acción y recurso

    private String deriveAction(String httpMethod, String methodPath) {
        if (httpMethod == null) {
            return "UNKNOWN";
        }
        if (methodPath != null && methodPath.endsWith("/status")) {
            return "STATUS_CHANGE";
        }
        return switch (httpMethod) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> httpMethod;
        };
    }

    /** /v1/catalogs/roles + /{id}/status -> catalogs.roles ; /v1/students/{studentId}/relatives -> students.relatives */
    private String deriveResource(String classPath, String methodPath) {
        String fullPath = (classPath == null ? "" : classPath) + "/" + (methodPath == null ? "" : methodPath);
        String resource = Arrays.stream(fullPath.split("/"))
                .filter(StringUtils::hasText)
                .filter(segment -> !segment.matches("v\\d+"))
                .filter(segment -> !segment.startsWith("{"))
                .filter(segment -> !"status".equals(segment))
                .collect(Collectors.joining("."));
        return resource.isEmpty() ? "unknown" : resource;
    }

    private String deriveCategory(String resource) {
        if (resource.startsWith("auth")) {
            return "authentication";
        }
        if (resource.startsWith("users") || resource.startsWith("students")) {
            return "iam";
        }
        return "database";
    }

    private String eventType(String action) {
        if (action.startsWith("LOGIN")) {
            return "start";
        }
        return switch (action) {
            case "CREATE" -> "creation";
            case "UPDATE", "STATUS_CHANGE" -> "change";
            case "DELETE" -> "deletion";
            default -> "info";
        };
    }

    // ---------------------------------------------------------------- actor

    private String resolveActor(Auditable auditable, Method method, Object[] args) {
        if (auditable != null && StringUtils.hasText(auditable.actor())) {
            try {
                MethodBasedEvaluationContext context =
                        new MethodBasedEvaluationContext(null, method, args, parameterNameDiscoverer);
                Object value = expressionParser.parseExpression(auditable.actor()).getValue(context);
                if (value != null && StringUtils.hasText(value.toString())) {
                    return LogFields.truncate(value.toString(), 150);
                }
            } catch (RuntimeException ignored) {
                // Expresión inválida o argumento nulo: se usa el usuario del contexto de seguridad.
            }
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return ANONYMOUS;
        }
        return authentication.getName();
    }

    // ---------------------------------------------------------------- datos de la petición

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes servletAttributes ? servletAttributes.getRequest() : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> pathVariables(HttpServletRequest request) {
        if (request == null) {
            return Collections.emptyMap();
        }
        Object variables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return variables instanceof Map<?, ?> map ? (Map<String, String>) map : Collections.emptyMap();
    }

    /** "id" si existe; si no, la última variable de la ruta (p. ej. relativeId en /students/{studentId}/relatives/{relativeId}). */
    private String resourceIdFromPath(Map<String, String> pathVariables) {
        if (pathVariables.isEmpty()) {
            return null;
        }
        if (pathVariables.containsKey("id")) {
            return pathVariables.get("id");
        }
        String last = null;
        for (String value : pathVariables.values()) {
            last = value;
        }
        return last;
    }

    private String declaredHttpMethod(Method method) {
        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
        return mapping != null && mapping.method().length > 0 ? mapping.method()[0].name() : null;
    }

    private String firstPath(RequestMapping mapping) {
        if (mapping == null || mapping.path().length == 0) {
            return null;
        }
        return mapping.path()[0];
    }

    // ---------------------------------------------------------------- lectura de DTOs (solo id y estado)

    /** Id del recurso en la respuesta: componente "id" o el primer componente numérico cuyo nombre empiece por "id". */
    private String extractId(Object result) {
        try {
            Object body = result instanceof ResponseEntity<?> entity ? entity.getBody() : result;
            if (body instanceof ApiResponse<?> envelope) {
                body = envelope.data();
            }
            if (body == null) {
                return null;
            }
            Class<?> type = body.getClass();
            if (type.isRecord()) {
                RecordComponent[] components = type.getRecordComponents();
                for (RecordComponent component : components) {
                    if ("id".equals(component.getName())) {
                        return stringValue(readComponent(component, body));
                    }
                }
                for (RecordComponent component : components) {
                    if (component.getName().startsWith("id") && isNumeric(component.getType())) {
                        return stringValue(readComponent(component, body));
                    }
                }
                return null;
            }
            Method getter = ReflectionUtils.findMethod(type, "getId");
            if (getter != null && getter.getParameterCount() == 0) {
                ReflectionUtils.makeAccessible(getter);
                return stringValue(ReflectionUtils.invokeMethod(getter, body));
            }
            return null;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    /** Valor del campo "activo" (StatusRequestDTO) o "status" (ReactivoStatusRequestDTO) del cuerpo de un cambio de estado. */
    private Object findNewStatus(Object[] args) {
        try {
            for (Object arg : args) {
                if (arg == null || !arg.getClass().isRecord()) {
                    continue;
                }
                for (RecordComponent component : arg.getClass().getRecordComponents()) {
                    if ("activo".equals(component.getName()) || "status".equals(component.getName())) {
                        return readComponent(component, arg);
                    }
                }
            }
        } catch (RuntimeException ex) {
            return null;
        }
        return null;
    }

    private Object readComponent(RecordComponent component, Object target) {
        Method accessor = component.getAccessor();
        ReflectionUtils.makeAccessible(accessor);
        return ReflectionUtils.invokeMethod(accessor, target);
    }

    private boolean isNumeric(Class<?> type) {
        return Number.class.isAssignableFrom(type) || type == long.class || type == int.class;
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private String firstNonNull(String first, String second) {
        return first != null ? first : second;
    }
}
