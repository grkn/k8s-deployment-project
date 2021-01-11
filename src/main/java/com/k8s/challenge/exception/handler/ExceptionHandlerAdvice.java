package com.k8s.challenge.exception.handler;

import com.k8s.challenge.constant.ExceptionResponse;
import com.k8s.challenge.exception.BadRequestException;
import com.k8s.challenge.exception.NotFoundException;
import com.k8s.challenge.resource.ErrorResource;
import io.kubernetes.client.openapi.ApiException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<?> handleApiException(ApiException ex) {
        if (!StringUtils.isEmpty(ex.getResponseBody())) {
            LOGGER.error(String.format("Api Exception occured. Response Body: %s", ex.getResponseBody()), ex);
            return ResponseEntity.badRequest().body(new ErrorResource(ExceptionResponse.BAD_REQUEST.getCode(),
                    String.format(ExceptionResponse.BAD_REQUEST.getMessage(), ex.getResponseBody())));
        } else {
            LOGGER.error(String.format("Api Exception occured. Response Body: %s", ex.getMessage()), ex);
            return ResponseEntity.status(ExceptionResponse.SERVER_ERROR.getStatus())
                    .body(new ErrorResource(ExceptionResponse.SERVER_ERROR.getCode(), ExceptionResponse.SERVER_ERROR.getMessage()));
        }
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ErrorResource> handleAccessDenied(AccessDeniedException ex) {
        LOGGER.error("User does not have any permission to this request.", ex);
        return ResponseEntity.status(ExceptionResponse.UNAUTHORIZED.getStatus())
                .body(new ErrorResource(ExceptionResponse.UNAUTHORIZED.getCode(),
                        ExceptionResponse.UNAUTHORIZED.getMessage()));
    }

    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<ErrorResource> handleBadRequest(BadRequestException ex) {
        LOGGER.error(ex.getMessage(), ex);
        return ResponseEntity.status(ExceptionResponse.BAD_REQUEST.getStatus())
                .body(new ErrorResource(ExceptionResponse.BAD_REQUEST.getCode(),
                        String.format(ExceptionResponse.BAD_REQUEST.getMessage(), ex.getMessage())));
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResource> handleMethodArgumentValidException(MethodArgumentNotValidException ex) {
        Optional<String> fieldName = findErrorFieldName(ex);
        LOGGER.error(String.format("Parameter %s can not be null or empty",
                fieldName.orElseGet(() -> "unknown")), ex);
        return ResponseEntity.status(ExceptionResponse.BAD_REQUEST.getStatus())
                .body(new ErrorResource(ExceptionResponse.BAD_REQUEST.getCode(),
                        String.format(ExceptionResponse.BAD_REQUEST.getMessage(),
                                String.format("Given parameter %s can not be null or empty", fieldName.orElseGet(() -> "unknown")))));
    }

    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<ErrorResource> handleNotFoundException(NotFoundException ex) {
        LOGGER.error(ex.getMessage(), ex);
        return ResponseEntity.status(ExceptionResponse.NOT_FOUND.getStatus())
                .body(new ErrorResource(ExceptionResponse.NOT_FOUND.getCode(),
                        String.format(ExceptionResponse.NOT_FOUND.getMessage(), ex.getMessage())));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResource> handleGeneralException(Exception ex) {
        LOGGER.error("Unknown Exception occured", ex);
        return ResponseEntity.badRequest().body(new ErrorResource(ExceptionResponse.GENERAL.getCode(),
                ExceptionResponse.GENERAL.getMessage()));
    }


    private Optional<String> findErrorFieldName(MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getField)
                .findFirst();
    }
}
