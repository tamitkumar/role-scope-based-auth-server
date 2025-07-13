package com.tech.brain.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    ERR000( "ERR.000", "The Web service authentication is invalid."),
    ERR001( "ERR.001", "Bad Request."),
    ERR002( "ERR.002", "user is null."),
    ERR003( "ERR.003", "Date format error."),
    ERR004( "ERR.004", "Internal server error."),
    ERR005( "ERR.005", "No @Id field found in entity: "),
    HTTP_CODE_500( "500", "Error when processing the request.");

    private final String errorCode;
    private final String errorMessage;

    ErrorCode(String code, String message) {
        this.errorCode = code;
        this.errorMessage = message;
    }

}
