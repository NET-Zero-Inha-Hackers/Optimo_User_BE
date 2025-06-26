package org.inhahackers.optmo_user_be.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AuthProvider {
    GOOGLE("GOOGLE"),
    KAKAO("KAKAO"),
    EMAIL("EMAIL");

    private String value;

    AuthProvider(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
