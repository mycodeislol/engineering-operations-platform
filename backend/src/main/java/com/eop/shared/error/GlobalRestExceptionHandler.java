package com.eop.shared.error;

import com.eop.operations.domain.exception.DeploymentNotFoundException;
import com.eop.operations.domain.exception.InvalidDeploymentStateTransitionException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalRestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalRestExceptionHandler.class);

    @ExceptionHandler(DeploymentNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleDeploymentNotFound(DeploymentNotFoundException ex, HttpServletRequest request) {
        log.warn("Deployment resource not found: [{}] at path: [{}]", ex.getMessage(), request.getRequestURI());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Deployment Resource Not Found");
        problem.setType(URI.create("https://eop.dev/errors/not-found"));
        if (ex.getDeploymentId() != null) {
            problem.setProperty("deploymentId", ex.getDeploymentId().value());
        }
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(InvalidDeploymentStateTransitionException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDeploymentTransition(InvalidDeploymentStateTransitionException ex, HttpServletRequest request) {
        log.error("Invalid deployment state transition: [{}] at path: [{}]", ex.getMessage(), request.getRequestURI());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Illegal State Machine Transition");
        problem.setType(URI.create("https://eop.dev/errors/illegal-state-transition"));
        if (ex.getDeploymentId() != null) {
            problem.setProperty("deploymentId", ex.getDeploymentId().value());
        }
        problem.setProperty("currentStatus", ex.getCurrentStatus());
        problem.setProperty("targetStatus", ex.getTargetStatus());
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(NoSuchElementException ex, HttpServletRequest request) {
        log.warn("Requested entity not found: [{}] at path: [{}]", ex.getMessage(), request.getRequestURI());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource Not Found");
        problem.setType(URI.create("https://eop.dev/errors/not-found"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        log.error("Illegal state or state transition conflict: [{}] at path: [{}]", ex.getMessage(), request.getRequestURI());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("State Conflict");
        problem.setType(URI.create("https://eop.dev/errors/state-conflict"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Invalid input argument: [{}] at path: [{}]", ex.getMessage(), request.getRequestURI());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Bad Request");
        problem.setType(URI.create("https://eop.dev/errors/bad-request"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation error encountered at path: [{}]", request.getRequestURI());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Payload failed constraint validation");
        problem.setTitle("Invalid Request Content");
        problem.setType(URI.create("https://eop.dev/errors/validation-failed"));

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("fieldErrors", errors);
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParams(org.springframework.web.bind.MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Missing parameter [{}] at path: [{}]", ex.getParameterName(), request.getRequestURI());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Missing Required Parameter");
        problem.setType(URI.create("https://eop.dev/errors/missing-parameter"));
        problem.setProperty("parameterName", ex.getParameterName());
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(jakarta.validation.ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Constraint violation at path: [{}]", request.getRequestURI());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Validation Failed");
        problem.setType(URI.create("https://eop.dev/errors/validation-failed"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(org.springframework.web.method.annotation.HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handleMethodValidation(org.springframework.web.method.annotation.HandlerMethodValidationException ex, HttpServletRequest request) {
        log.warn("Method validation failed at path: [{}]", request.getRequestURI());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed for request parameters");
        problem.setTitle("Validation Failed");
        problem.setType(URI.create("https://eop.dev/errors/validation-failed"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnhandledException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled internal server exception caught at: [{}]", request.getRequestURI(), ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal error occurred. Please contact platform operations.");
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://eop.dev/errors/internal-server-error"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
