package org.example.exception;

import org.example.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

/**
 * Globalny handler wyjątków dla całej aplikacji.
 * Zapewnia spójne i przyjazne dla użytkownika komunikaty o błędach w formacie JSON.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Obsługa wyjątku gdy nie znaleziono encji
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("Entity not found [RequestId: {}]: {}", requestId, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .requestId(requestId)
                .errorCode("ENTITY_NOT_FOUND")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Obsługa wyjątków biznesowych
     */
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ErrorResponse> handleBusinessLogicException(
            BusinessLogicException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("Business logic error [RequestId: {}]: {}", requestId, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Business Logic Error")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .requestId(requestId)
                .errorCode(ex.getErrorCode())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Obsługa wyjątków walidacji biznesowej
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("Validation error [RequestId: {}]: {}", requestId, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .requestId(requestId)
                .errorCode("VALIDATION_ERROR")
                .fieldErrors(ex.getFieldErrors())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Obsługa wyjątków walidacji Bean Validation (JSR-303)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("Method argument validation failed [RequestId: {}]", requestId);

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Request validation failed")
                .path(request.getRequestURI())
                .requestId(requestId)
                .errorCode("REQUEST_VALIDATION_ERROR")
                .fieldErrors(fieldErrors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Obsługa błędów parsowania JSON
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("JSON parsing error [RequestId: {}]: {}", requestId, ex.getMessage());

        String message = "Invalid JSON format or structure";
        if (ex.getCause() != null) {
            String causeMessage = ex.getCause().getMessage();
            if (causeMessage != null && causeMessage.contains("Cannot deserialize")) {
                message = "Invalid data format in request";
            }
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .path(request.getRequestURI())
                .requestId(requestId)
                .errorCode("INVALID_JSON")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Obsługa błędów konwersji typów w parametrach
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("Method argument type mismatch [RequestId: {}]: {}", requestId, ex.getMessage());

        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", 
                                      ex.getValue(), ex.getName(), 
                                      ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .path(request.getRequestURI())
                .requestId(requestId)
                .errorCode("INVALID_PARAMETER_TYPE")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Obsługa błędu 404 - nie znaleziono zasobu
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("Resource not found [RequestId: {}]: {}", requestId, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message("The requested resource was not found")
                .path(request.getRequestURI())
                .requestId(requestId)
                .errorCode("RESOURCE_NOT_FOUND")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Obsługa błędów dostępu i autoryzacji
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("Illegal argument [RequestId: {}]: {}", requestId, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .requestId(requestId)
                .errorCode("ILLEGAL_ARGUMENT")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Obsługa błędów stanu - operacje niedozwolone w danym stanie
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("Illegal state [RequestId: {}]: {}", requestId, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .requestId(requestId)
                .errorCode("ILLEGAL_STATE")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Obsługa wyjątków związanych z bazą danych
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            org.springframework.dao.DataIntegrityViolationException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.warn("Data integrity violation [RequestId: {}]: {}", requestId, ex.getMessage());

        String message = "Data integrity constraint violation";
        String errorCode = "DATA_INTEGRITY_ERROR";

        // Analiza konkretnego błędu
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("UNIQUE") || ex.getMessage().contains("Duplicate")) {
                message = "Resource with this identifier already exists";
                errorCode = "DUPLICATE_RESOURCE";
            } else if (ex.getMessage().contains("FOREIGN KEY") || ex.getMessage().contains("referenced")) {
                message = "Cannot delete resource - it is referenced by other data";
                errorCode = "RESOURCE_REFERENCED";
            }
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(message)
                .path(request.getRequestURI())
                .requestId(requestId)
                .errorCode(errorCode)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Obsługa wszystkich pozostałych wyjątków
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        logger.error("Unexpected error [RequestId: {}]: {}", requestId, ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getRequestURI())
                .requestId(requestId)
                .errorCode("INTERNAL_ERROR")
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Generuje unikalny identyfikator żądania do śledzenia błędów
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
} 