package com.devapix.sandbox_service.client;

import com.devapix.sandbox_service.dto.ApiExecutionDataDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "api-catalog-service", url = "http://localhost:8082")
public interface ApiCatalogClient {

    @GetMapping("/api-catalog/internal/apis/{apiId}/endpoints/{endpointId}")
    ApiExecutionDataDTO getExecutionData(@PathVariable("apiId") Integer apiId, @PathVariable("endpointId") Integer endpointId);

    @GetMapping("/api-catalog/internal/apis/{apiId}")
    com.devapix.sandbox_service.dto.ApiCatalogResponse getApiById(@PathVariable("apiId") Integer apiId);
}
