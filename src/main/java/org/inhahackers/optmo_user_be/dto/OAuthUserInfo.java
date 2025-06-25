package org.inhahackers.optmo_user_be.dto;

import lombok.Builder;
import lombok.Getter;
import org.inhahackers.optmo_user_be.entity.AuthProvider;

@Getter
@Builder
public class OAuthUserInfo {
    private String email;
    private String name;
    private String profileImage;
    private AuthProvider provider;
    private String providerId;

    public UserOAuthRequest toUserOAuthRequest() {
        return UserOAuthRequest.builder()
                .email(this.email)
                .name(this.name)
                .profileImage(this.profileImage)
                .provider(this.provider)
                .providerId(this.providerId)
                .build();
    }
}
