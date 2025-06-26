package org.inhahackers.optmo_user_be.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailRequest {
    private String email;
}
