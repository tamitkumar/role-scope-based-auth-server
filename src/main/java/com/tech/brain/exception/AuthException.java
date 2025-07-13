package com.tech.brain.exception;

import lombok.Getter;

import java.io.Serial;

import static com.tech.brain.utils.AuthConstant.HTTP_CODE_500;
import static com.tech.brain.utils.AuthConstant.HYPHEN;

@Getter
public class AuthException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -4711404730678356597L;

    private final String code;
    private final String message;

    public AuthException(Exception exception) {
        super(exception);
        String builder = ErrorCode.HTTP_CODE_500.getErrorCode() + HYPHEN +
                ErrorSeverity.FATAL + HYPHEN + ErrorCode.HTTP_CODE_500.getErrorMessage();
        this.code = HTTP_CODE_500;
        this.message = builder;
    }

    public AuthException(String code, String message) {
        super(message);
        String builder = code + HYPHEN + message;
        this.code = code;
        this.message = builder;
    }

    public AuthException(String code, ErrorSeverity severity, String message) {
        super(message);
        String builder = code + HYPHEN + severity + HYPHEN + message;
        this.code = code;
        this.message = builder;
    }

    public AuthException(String code, ErrorSeverity severity, String message, Exception exception) {
        super(exception);
        String builder = code + HYPHEN + severity + HYPHEN + message;
        this.code = code;
        this.message = builder;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
