package com.devapix.sandbox_service.dto;

import lombok.Data;

@Data
public class ApiExecutionDataDTO {

    private String baseUrl;
    private String endpoint;
    private String method;
    private String headersJson;
    private String paramsJson;
    private String sampleRequest;
}
