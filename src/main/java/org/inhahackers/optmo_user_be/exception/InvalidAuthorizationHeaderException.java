package org.inhahackers.optmo_user_be.exception;

public class InvalidAuthorizationHeaderException extends RuntimeException {
    public InvalidAuthorizationHeaderException(String message) {
        super(message);
    }
}