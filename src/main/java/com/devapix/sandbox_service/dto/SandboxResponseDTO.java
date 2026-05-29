package com.devapix.sandbox_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SandboxResponseDTO {

    private Integer statusCode;
    private String responseBody;
    private Long latencyMs;
    private Integer responseSize;
    private String errorMessage;
}