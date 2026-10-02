package com.hosteleriapractica.backendpractica.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ex.getStatus().value());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }
    
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegrityViolation(org.springframework.dao.DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(body(409, "No se puede eliminar: está siendo usado en comandas existentes"));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(body(400, mensaje));
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        String mensaje = ex instanceof DisabledException
                ? "El usuario está desactivado"
                : "Usuario o contraseña incorrectos";
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body(401, mensaje));
    }

    private Map<String, Object> body(int status, String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("timestamp", LocalDateTime.now());
        map.put("status", status);
        map.put("message", message);
        return map;
    }
}