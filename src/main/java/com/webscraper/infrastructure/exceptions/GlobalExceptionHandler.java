package com.webscraper.infrastructure.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URISyntaxException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(URISyntaxException.class)
    protected ResponseEntity<String> handleBadRequest(URISyntaxException ex) {
        log.error("Invalid URL format: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body("Invalid URL format: " + ex.getMessage());
    }

}