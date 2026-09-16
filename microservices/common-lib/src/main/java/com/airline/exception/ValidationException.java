package com.airline.exception;

/**
 * Excepción lanzada cuando una validación de negocio falla.
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }

    public ValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

}