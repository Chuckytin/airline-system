package com.airline.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String resourceName, Object id) {
        super(errorCode, String.format("%s not found with id: %s", resourceName, id));
    }

}