package com.airline.exception;

/**
 * Excepción lanzada cuando se intenta crear un recurso que ya existe.
 */
public class ResourceAlreadyExistsException extends BusinessException {

    public ResourceAlreadyExistsException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ResourceAlreadyExistsException(ErrorCode errorCode, String resourceName, String field, Object value) {
        super(errorCode, String.format("%s already exists with %s: %s", resourceName, field, value));
    }

}