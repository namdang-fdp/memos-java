package com.namdang.memos.exception;

import com.namdang.memos.dto.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<?>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ApiResponse<?> response = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity
                .status(errorCode.getStatusCode().value())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnexpectedError(Exception ex) {
        log.error("Error:", ex);
        ApiResponse<?> response = ApiResponse.builder()
                .code(ErrorCode.UNCATEGORY_EXCEPTION.getCode())
                .message("Something went wrong, contact admin")
                .build();
        return ResponseEntity
                .status(ErrorCode.UNCATEGORY_EXCEPTION.getStatusCode().value())
                .body(response);
    }
}
