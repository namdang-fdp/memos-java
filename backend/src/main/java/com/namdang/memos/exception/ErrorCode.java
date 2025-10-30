package com.namdang.memos.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORY_EXCEPTION(9999, "Uncategory Exception", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED_EXCEPTION(1001, "Unauthenticated", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1002, "Invalid email", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1003, "Invalid password", HttpStatus.BAD_REQUEST),
    DUPLICATE_EMAIL(1004, "This email already exist", HttpStatus.BAD_REQUEST),
    INVALID_ACCOUNT(1005, "Cannot find this email", HttpStatus.BAD_REQUEST),

    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

}
