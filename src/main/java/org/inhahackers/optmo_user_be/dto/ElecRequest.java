package org.inhahackers.optmo_user_be.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ElecRequest {
    private Long useElecEstimate;
    private Long llmElecEstimate;
}
