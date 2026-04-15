package com.example.openclaw.web;

import com.example.openclaw.client.OpenClawGatewayException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class OpenClawExceptionHandler {

    @ExceptionHandler(OpenClawGatewayException.class)
    public ResponseEntity<Map<String, Object>> handleOpenClawGatewayException(OpenClawGatewayException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of(
                        "error", "openclaw_upstream_error",
                        "message", ex.getMessage(),
                        "upstreamStatus", ex.getUpstreamStatus(),
                        "upstreamPath", ex.getUpstreamPath(),
                        "upstreamBody", ex.getUpstreamBody() == null ? "" : ex.getUpstreamBody()
                ));
    }
}
