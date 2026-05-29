package com.devapix.sandbox_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecordUsageRequestDTO {

    private String apiKey;
    private String endpoint;
    private Integer statusCode;
    private Long responseTimeMs;
    private String httpMethod;
    private String ipAddress;
}
