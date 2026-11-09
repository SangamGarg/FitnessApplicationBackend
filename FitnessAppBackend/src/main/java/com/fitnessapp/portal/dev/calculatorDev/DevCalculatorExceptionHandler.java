package com.fitnessapp.portal.dev.calculatorDev;

import com.fitnessapp.portal.dev.calculatorDev.controller.DevCalculatorController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Central place where persistence failures from {@link DevCalculatorController} are logged and mapped.
 * Scoped to that controller only, and deliberately narrow (no {@code Exception.class} handler) so
 * Spring's own validation / 4xx handling is not swallowed.
 */
@Slf4j
@RestControllerAdvice(assignableTypes = DevCalculatorController.class)
public class DevCalculatorExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Dev calculator request violated a data constraint: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Request conflicts with existing data");
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> handleDataAccess(DataAccessException ex) {
        log.error("Dev calculator request failed with a database error: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body("Database error while processing request");
    }
}
