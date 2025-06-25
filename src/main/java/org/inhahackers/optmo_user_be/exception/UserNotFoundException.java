package org.inhahackers.optmo_user_be.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
