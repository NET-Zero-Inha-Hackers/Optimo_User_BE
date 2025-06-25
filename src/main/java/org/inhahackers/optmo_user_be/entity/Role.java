package org.inhahackers.optmo_user_be.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    ROLE_USER("ROLE_USER"),
    ROLE_DEMO("ROLE_DEMO"),
    ROLE_ADMIN("ROLE_ADMIN");

    private String role;

    Role(String role) {
        this.role = role;
    }

    @JsonValue
    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return role;
    }
}
