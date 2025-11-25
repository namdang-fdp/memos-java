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
    MISSING_ORY_COOKIES(1006, "Missing ory cookies", HttpStatus.BAD_REQUEST),
    INVALID_ORY_COOKIES(1007, "Invalid ory cookies", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1008, "Role not found", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXIST(1009, "Email already exist", HttpStatus.BAD_REQUEST),
    MISSING_REFRESH_TOKEN_COOKIE(1010, "Missing refresh token in cookies", HttpStatus.BAD_REQUEST),
    MISSING_AUTH_HEADER(1011, "Missing access token", HttpStatus.BAD_REQUEST),
    INVALID_PROJECT_NAME(1012, "Invalid project name", HttpStatus.BAD_REQUEST),
    PROJECT_NOT_FOUND(1013, "Project not found", HttpStatus.BAD_REQUEST),
    FORBIDDEN_ACTION(1014, "You cannot do this action",  HttpStatus.FORBIDDEN),
    PROJECT_ALREADY_ARCHIVED(1015, "Project is already archived", HttpStatus.BAD_REQUEST)
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
