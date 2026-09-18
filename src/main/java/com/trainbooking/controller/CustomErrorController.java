package com.trainbooking.controller;

import com.trainbooking.dto.response.ApiResponse;
import com.trainbooking.exception.ErrorCode;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ApiResponse<Void>> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        String uri = (requestUri != null) ? requestUri.toString() : "";

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.<Void>builder()
                                .code(ErrorCode.ENDPOINT_NOT_FOUND.getCode())
                                .message(ErrorCode.ENDPOINT_NOT_FOUND.getMessage() + (uri.isEmpty() ? "" : ": " + uri))
                                .build()
                );
            } else if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.<Void>builder()
                                .code(ErrorCode.UNAUTHENTICATED.getCode())
                                .message(ErrorCode.UNAUTHENTICATED.getMessage())
                                .build()
                );
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.<Void>builder()
                                .code(ErrorCode.UNAUTHORIZED.getCode())
                                .message(ErrorCode.UNAUTHORIZED.getMessage())
                                .build()
                );
            }
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.<Void>builder()
                        .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                        .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
                        .build()
        );
    }
}
