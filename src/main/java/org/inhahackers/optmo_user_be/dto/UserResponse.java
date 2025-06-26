package org.inhahackers.optmo_user_be.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.inhahackers.optmo_user_be.entity.AuthProvider;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String profileImage;
    private AuthProvider provider;
    private Long totalUseElecEstimate;
    private Long totalLlmElecEstimate;
}
