package com.airline.exception;

import lombok.Getter;

/**
 * Excepción base para todas las excepciones de negocio.
 * - Cada excepción debe heredar esta clase o alguna de sus subclases:
 * ({@link ResourceNotFoundException}, {@link ResourceAlreadyExistsException}, {@link ValidationException})
 * ---
 * El {@link ErrorCode} asociado determina:
 * - El código de error devuelto en la respuesta.
 * - El status HTTP de la respuesta.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}