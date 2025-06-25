package org.inhahackers.optmo_user_be.exception;

public class OAuthTokenValidationException extends RuntimeException {

    public OAuthTokenValidationException(String message) {
        super(message);
    }

    public OAuthTokenValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
