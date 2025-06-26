package org.inhahackers.optmo_user_be.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ElecAndCostRequest {
    private Long useElecEstimate;
    private Long llmElecEstimate;
    private Long useCostEstimate;
    private Long llmCostEstimate;
}
