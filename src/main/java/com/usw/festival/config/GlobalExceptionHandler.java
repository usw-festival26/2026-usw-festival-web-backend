package com.usw.festival.config;

import com.usw.festival.dto.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String NOT_FOUND = "NOT_FOUND";
    private static final String BAD_REQUEST = "BAD_REQUEST";
    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    private static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    private static final String INVALID_REQUEST_MESSAGE = "잘못된 요청입니다.";
    private static final String VALIDATION_ERROR_MESSAGE = "요청 값이 올바르지 않습니다.";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "서버 내부 오류가 발생했습니다.";
    private static final String NO_RESOURCE_FOUND_MESSAGE = "존재하지 않는 요청 경로입니다.";

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElement(NoSuchElementException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, NOT_FOUND, e.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException e, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, NOT_FOUND, NO_RESOURCE_FOUND_MESSAGE, request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                      HttpServletRequest request) {
        return buildValidationResponse(extractFieldErrors(e.getFieldErrors()), request.getRequestURI());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e, HttpServletRequest request) {
        return buildValidationResponse(extractFieldErrors(e.getFieldErrors()), request.getRequestURI());
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException e,
                                                                       HttpServletRequest request) {
        return buildValidationResponse(extractFieldErrors(e), request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e,
                                                                   HttpServletRequest request) {
        return buildValidationResponse(extractFieldErrors(e), request.getRequestURI());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, BAD_REQUEST, INVALID_REQUEST_MESSAGE, request.getRequestURI());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e,
                                                                 HttpServletRequest request) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "지원하지 않는 HTTP 메서드입니다.", request.getRequestURI());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException e,
                                                                     HttpServletRequest request) {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                "지원하지 않는 Content-Type입니다.", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        if (e instanceof AuthenticationException authenticationException) {
            throw authenticationException;
        }
        if (e instanceof AccessDeniedException accessDeniedException) {
            throw accessDeniedException;
        }

        log.error(
                "Unhandled exception occurred. path={}, exception={}",
                request.getRequestURI(),
                e.getClass().getName(),
                e
        );
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                INTERNAL_SERVER_ERROR,
                INTERNAL_SERVER_ERROR_MESSAGE,
                request.getRequestURI()
        );
    }

    private ResponseEntity<ErrorResponse> buildValidationResponse(List<ErrorResponse.FieldErrorDetail> fieldErrors,
                                                                  String path) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        VALIDATION_ERROR,
                        VALIDATION_ERROR_MESSAGE,
                        path,
                        fieldErrors
                ));
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code, String message, String path) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status.value(), code, message, path));
    }

    private List<ErrorResponse.FieldErrorDetail> extractFieldErrors(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(fieldError -> new ErrorResponse.FieldErrorDetail(
                        fieldError.getField(),
                        resolveMessage(fieldError)
                ))
                .toList();
    }

    private List<ErrorResponse.FieldErrorDetail> extractFieldErrors(HandlerMethodValidationException exception) {
        List<ErrorResponse.FieldErrorDetail> fieldErrors = new ArrayList<>();

        for (ParameterValidationResult validationResult : exception.getParameterValidationResults()) {
            if (validationResult instanceof ParameterErrors parameterErrors && parameterErrors.hasFieldErrors()) {
                parameterErrors.getFieldErrors().forEach(fieldError -> fieldErrors.add(
                        new ErrorResponse.FieldErrorDetail(fieldError.getField(), resolveMessage(fieldError))
                ));
                continue;
            }

            String field = resolveParameterName(validationResult);
            validationResult.getResolvableErrors().forEach(error -> fieldErrors.add(
                    new ErrorResponse.FieldErrorDetail(field, resolveMessage(error))
            ));
        }

        exception.getCrossParameterValidationResults().forEach(error -> fieldErrors.add(
                new ErrorResponse.FieldErrorDetail("request", resolveMessage(error))
        ));

        return fieldErrors;
    }

    private List<ErrorResponse.FieldErrorDetail> extractFieldErrors(ConstraintViolationException exception) {
        return exception.getConstraintViolations().stream()
                .map(violation -> new ErrorResponse.FieldErrorDetail(
                        resolveFieldName(violation),
                        violation.getMessage()
                ))
                .toList();
    }

    private String resolveParameterName(ParameterValidationResult validationResult) {
        RequestParam requestParam = validationResult.getMethodParameter().getParameterAnnotation(RequestParam.class);
        if (requestParam != null) {
            if (StringUtils.hasText(requestParam.name())) {
                return requestParam.name();
            }
            if (StringUtils.hasText(requestParam.value())) {
                return requestParam.value();
            }
        }

        PathVariable pathVariable = validationResult.getMethodParameter().getParameterAnnotation(PathVariable.class);
        if (pathVariable != null) {
            if (StringUtils.hasText(pathVariable.name())) {
                return pathVariable.name();
            }
            if (StringUtils.hasText(pathVariable.value())) {
                return pathVariable.value();
            }
        }

        RequestHeader requestHeader = validationResult.getMethodParameter().getParameterAnnotation(RequestHeader.class);
        if (requestHeader != null) {
            if (StringUtils.hasText(requestHeader.name())) {
                return requestHeader.name();
            }
            if (StringUtils.hasText(requestHeader.value())) {
                return requestHeader.value();
            }
        }

        return Optional.ofNullable(validationResult.getMethodParameter().getParameterName())
                .orElse("request");
    }

    private String resolveFieldName(ConstraintViolation<?> violation) {
        String fieldName = null;

        for (Path.Node node : violation.getPropertyPath()) {
            if (StringUtils.hasText(node.getName())) {
                fieldName = node.getName();
            }
        }

        return fieldName != null ? fieldName : "request";
    }

    private String resolveMessage(MessageSourceResolvable resolvable) {
        return Optional.ofNullable(resolvable.getDefaultMessage())
                .orElse(VALIDATION_ERROR_MESSAGE);
    }
}
