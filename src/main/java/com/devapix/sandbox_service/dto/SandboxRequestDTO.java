    package com.devapix.sandbox_service.dto;

    import lombok.Data;
    import java.util.Map;

    @Data
    public class SandboxRequestDTO {

        private Integer apiId;
        private Integer endpointId;
        private Integer userId;
        private String apiKey;
    }
