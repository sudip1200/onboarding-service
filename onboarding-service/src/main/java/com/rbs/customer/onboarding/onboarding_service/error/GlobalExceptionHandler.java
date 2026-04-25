package com.rbs.customer.onboarding.onboarding_service.error;

import com.rbs.customer.onboarding.onboarding_service.observability.CorrelationIdFilter;
import com.rbs.customer.onboarding.onboarding_service.observability.FailureSignal;
import com.rbs.customer.onboarding.onboarding_service.observability.FailureSignalPublisher;
import com.rbs.customer.onboarding.onboarding_service.incident.IncidentAutomationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final FailureSignalPublisher failureSignalPublisher;
    private final IncidentAutomationService incidentAutomationService;

    @ExceptionHandler(OnboardingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(OnboardingNotFoundException ex, HttpServletRequest request) {
        return buildResponse("ONBOARDING_NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND, request, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::fieldMessage)
                .collect(Collectors.joining(", "));

        return buildResponse("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST, request, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return buildResponse("INTERNAL_SERVER_ERROR", "Unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR, request, ex);
    }

    private String fieldMessage(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            String code,
            String message,
            HttpStatus status,
            HttpServletRequest request,
            Exception ex
    ) {
        String correlationId = currentCorrelationId();
        log.error(
                "request.failed code={} status={} correlationId={} endpoint={} type={} message={}",
                code,
                status.value(),
                correlationId,
                request.getMethod() + " " + request.getRequestURI(),
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );

        FailureSignal signal = new FailureSignal(
                correlationId,
                request.getMethod() + " " + request.getRequestURI(),
                code,
                status.value(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                Instant.now()
        );
        failureSignalPublisher.publish(signal);
        incidentAutomationService.process(signal);

        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(code, message, correlationId, Instant.now()));
    }

    private String currentCorrelationId() {
        String value = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
        return value == null ? "missing-correlation-id" : value;
    }
}
