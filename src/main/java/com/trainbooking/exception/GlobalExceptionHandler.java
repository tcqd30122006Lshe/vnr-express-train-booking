package com.trainbooking.exception;

import com.trainbooking.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = NoResourceFoundException.class)
    ResponseEntity<ApiResponse<?>> handlingNoResourceFoundException(NoResourceFoundException exception) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(ErrorCode.ENDPOINT_NOT_FOUND.getCode())
                .message("Đường dẫn không tồn tại trên hệ thống: /" + exception.getResourcePath())
                .build();

        return ResponseEntity.status(ErrorCode.ENDPOINT_NOT_FOUND.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = org.springframework.security.oauth2.jwt.JwtException.class)
    ResponseEntity<ApiResponse<?>> handlingJwtException(org.springframework.security.oauth2.jwt.JwtException exception) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(ErrorCode.UNAUTHENTICATED.getCode())
                .message(ErrorCode.UNAUTHENTICATED.getMessage())
                .build();
        return ResponseEntity.status(ErrorCode.UNAUTHENTICATED.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = org.springframework.security.access.AccessDeniedException.class)
    ResponseEntity<ApiResponse<?>> handlingAccessDeniedException(org.springframework.security.access.AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(value = org.springframework.http.converter.HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<?>> handlingHttpMessageNotReadableException(org.springframework.http.converter.HttpMessageNotReadableException exception) {
        log.warn("Malformed JSON or invalid types: {}", exception.getMessage());
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(ErrorCode.INVALID_KEY.getCode())
                .message("Dữ liệu gửi lên không đúng định dạng JSON hoặc sai kiểu dữ liệu")
                .build();
        return ResponseEntity.status(ErrorCode.INVALID_KEY.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = org.springframework.dao.DataIntegrityViolationException.class)
    ResponseEntity<ApiResponse<?>> handlingDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException exception) {
        log.warn("Database constraint violation: {}", exception.getMessage());
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(ErrorCode.INVALID_KEY.getCode())
                .message("Dữ liệu bị trùng lặp hoặc vi phạm ràng buộc cơ sở dữ liệu")
                .build();
        return ResponseEntity.status(ErrorCode.INVALID_KEY.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    ResponseEntity<ApiResponse<?>> handlingIllegalArgumentException(IllegalArgumentException exception) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(ErrorCode.INVALID_KEY.getCode())
                .message(exception.getMessage() != null ? exception.getMessage() : ErrorCode.INVALID_KEY.getMessage())
                .build();
        return ResponseEntity.status(ErrorCode.INVALID_KEY.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<?>> handlingRuntimeException(Exception exception) {
        log.error("Unhandled exception occurred: ", exception);
        String message = (exception.getMessage() != null && !exception.getMessage().isBlank())
                ? exception.getMessage()
                : ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage();
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                .message(message)
                .build();

        return ResponseEntity.status(ErrorCode.UNCATEGORIZED_EXCEPTION.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<?>> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        String message = (exception.getMessage() != null && !exception.getMessage().isEmpty())
                ? exception.getMessage()
                : errorCode.getMessage();
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(message)
                .build();

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<?>> handlingValidation(MethodArgumentNotValidException exception) {
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        String message = "Dữ liệu không hợp lệ";

        if (exception.getFieldError() != null) {
            String enumKey = exception.getFieldError().getDefaultMessage();
            if (enumKey != null) {
                try {
                    errorCode = ErrorCode.valueOf(enumKey);
                    message = errorCode.getMessage();
                } catch (IllegalArgumentException e) {
                    message = enumKey;
                }
            }
        }

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(message)
                .build();

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }
}
