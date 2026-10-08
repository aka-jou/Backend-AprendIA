package com.aprendia.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        logger.warn("Recurso no encontrado: {}", ex.getMessage());
        ErrorItem errorItem = ErrorItem.builder()
                .status("404")
                .code("ERR_NOT_FOUND")
                .title("Not Found")
                .detail(ex.getMessage())
                .build();
                
        ErrorResponse response = ErrorResponse.builder()
                .errors(List.of(errorItem))
                .build();
                
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        logger.warn("Petición incorrecta: {}", ex.getMessage());
        ErrorItem errorItem = ErrorItem.builder()
                .status("400")
                .code("ERR_BAD_REQUEST")
                .title("Bad Request")
                .detail(ex.getMessage())
                .build();
                
        ErrorResponse response = ErrorResponse.builder()
                .errors(List.of(errorItem))
                .build();
                
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        logger.warn("Error de validación en los argumentos de la petición");
        List<ErrorItem> errorItems = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ErrorItem.builder()
                        .status("422")
                        .code("ERR_VAL_" + error.getField().toUpperCase())
                        .title("Unprocessable Entity")
                        .detail(error.getDefaultMessage())
                        .source(ErrorSource.builder()
                                .pointer("/data/attributes/" + error.getField())
                                .build())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse response = ErrorResponse.builder()
                .errors(errorItems)
                .build();
                
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        logger.warn("Acceso denegado: {}", ex.getMessage());
        ErrorItem errorItem = ErrorItem.builder()
                .status("403")
                .code("ERR_FORBIDDEN")
                .title("Forbidden")
                .detail("No tiene permisos para acceder a este recurso.")
                .build();
                
        ErrorResponse response = ErrorResponse.builder()
                .errors(List.of(errorItem))
                .build();
                
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        logger.warn("Cuerpo de petición ilegible o tipo de dato incorrecto: {}", ex.getMessage());
        
        String detailMsg = "El formato de los datos enviados es incorrecto o el JSON está mal formado.";
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            if (!ife.getPath().isEmpty()) {
                String fieldName = ife.getPath().get(0).getFieldName();
                String targetType = ife.getTargetType().getSimpleName();
                detailMsg = "Tipo de dato incorrecto, el campo '" + fieldName + "' debe ser de tipo " + targetType + ".";
            }
        }

        ErrorItem errorItem = ErrorItem.builder()
                .status("400")
                .code("ERR_BAD_REQUEST_FORMAT")
                .title("Bad Request Format")
                .detail(detailMsg)
                .build();
                
        ErrorResponse response = ErrorResponse.builder()
                .errors(List.of(errorItem))
                .build();
                
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        logger.warn("Violación de integridad de datos: {}", ex.getMessage());
        
        ErrorItem errorItem = ErrorItem.builder()
                .status("409")
                .code("ERR_DATA_INTEGRITY")
                .title("Data Integrity Conflict")
                .detail("No se pudo completar la operación debido a un problema de integridad de datos (registro inexistente o duplicado).")
                .build();
                
        ErrorResponse response = ErrorResponse.builder()
                .errors(List.of(errorItem))
                .build();
                
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        logger.error("Error no controlado del sistema: ", ex);
        ErrorItem errorItem = ErrorItem.builder()
                .status("500")
                .code("ERR_INTERNAL_SERVER")
                .title("Internal Server Error")
                .detail("Ocurrió un error interno en el servidor. Por favor, intente más tarde.")
                .build();
                
        ErrorResponse response = ErrorResponse.builder()
                .errors(List.of(errorItem))
                .build();
                
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
