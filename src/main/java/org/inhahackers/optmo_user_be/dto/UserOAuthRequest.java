package org.inhahackers.optmo_user_be.dto;

import lombok.Builder;
import lombok.Getter;
import org.inhahackers.optmo_user_be.entity.AuthProvider;

@Getter
@Builder
public class UserOAuthRequest {
    private String email;
    private String name;
    private String profileImage;
    private AuthProvider provider;
    private String providerId;
}
