package com.aibusinessadvisor.backend.advisor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;


@RestControllerAdvice
public class AdvisorExceptionHandler {


    @ExceptionHandler(AdvisorServiceException.class)
    public ResponseEntity<Map<String, Object>> handle(
            AdvisorServiceException exception
    ) {

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "timestamp",
                Instant.now().toString()
        );

        body.put(
                "status",
                HttpStatus.BAD_GATEWAY.value()
        );

        body.put(
                "error",
                "AI Advisor service error"
        );

        body.put(
                "message",
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(body);
    }
}
