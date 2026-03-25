package com.library.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " con ID " + id + " no encontrado");
    }
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
