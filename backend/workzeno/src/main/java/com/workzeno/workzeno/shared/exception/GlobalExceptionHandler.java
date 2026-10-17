package com.workzeno.workzeno.shared.exception;
import com.workzeno.workzeno.shared.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler
        extends ResponseEntityExceptionHandler {

    private ProblemDetail createProblem(
            HttpStatusCode status,
            String title,
            String detail,
            HttpServletRequest request
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(status, detail);

        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));

        Object requestId = request.getAttribute(
                RequestIdFilter.REQUEST_ID_ATTRIBUTE
        );

        if (requestId != null) {
            problem.setProperty("requestId", requestId.toString());
        }

        return problem;
    }

    private ResponseEntity<ProblemDetail> response(
            ProblemDetail problem
    ) {
        return ResponseEntity
                .status(problem.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ProblemDetail> handleApiException(
            ApiException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = createProblem(
                exception.getStatus(),
                exception.getTitle(),
                exception.getMessage(),
                request
        );

        return response(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error(
                "Unexpected error processing request {}",
                request.getRequestURI(),
                exception
        );

        ProblemDetail problem = createProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "An unexpected error occurred",
                request
        );

        return response(problem);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest webRequest
    ) {
        HttpServletRequest request =
                ((org.springframework.web.context.request.ServletWebRequest)
                        webRequest).getRequest();

        List<ValidationError> errors =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(this::toValidationError)
                        .toList();

        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                "One or more fields are invalid",
                request
        );

        problem.setProperty("errors", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    private ValidationError toValidationError(FieldError error) {
        return new ValidationError(
                error.getField(),
                error.getDefaultMessage() == null
                        ? "Invalid value"
                        : error.getDefaultMessage()
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest webRequest
    ) {
        HttpServletRequest request =
                ((org.springframework.web.context.request.ServletWebRequest)
                        webRequest).getRequest();

        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Malformed request",
                "The request body is missing or contains invalid JSON",
                request
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}

