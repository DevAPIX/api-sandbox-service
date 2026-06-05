package com.devapix.sandbox_service.service.impl;

import com.devapix.sandbox_service.client.ApiCatalogClient;
import com.devapix.sandbox_service.client.SubscriptionServiceClient;
import com.devapix.sandbox_service.config.ErrorMessages;
import com.devapix.sandbox_service.dto.*;
import com.devapix.sandbox_service.model.SandboxRequest;
import com.devapix.sandbox_service.model.SandboxResponse;
import com.devapix.sandbox_service.repo.SandboxRequestRepo;
import com.devapix.sandbox_service.repo.SandboxResponseRepo;
import com.devapix.sandbox_service.service.SandboxService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class SandboxServiceImpl implements SandboxService {

    private final ApiCatalogClient apiCatalogClient;
    private final SubscriptionServiceClient subscriptionServiceClient;
    private final SandboxRequestRepo requestRepo;
    private final SandboxResponseRepo responseRepo;
    private final ObjectMapper objectMapper;
    private final ErrorMessages errorMessages;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SandboxResponseDTO execute(SandboxRequestDTO dto) {

        log.info("Execution started apiId={} endpointId={} apiKey={}", dto.getApiId(), dto.getEndpointId(), dto.getApiKey() != null ? "present" : "missing");
        int statusCode = 500;
        long latency = 0;
        String responseBody = null;
        String endpoint = "/unknown";
        String method = "GET";
        String url = "unknown";

        try {
            log.info("Fetching API details from API Catalog for apiId={}", dto.getApiId());
            com.devapix.sandbox_service.dto.ApiCatalogResponse api = apiCatalogClient.getApiById(dto.getApiId());
            if (api == null) {
                log.error("API Catalog returned null for apiId={}", dto.getApiId());
                return new SandboxResponseDTO(404, null, 0L, 0, errorMessages.getApiNotFound());
            }

            Integer userId = dto.getUserId();
            boolean isOwner = (api.getOwnerId() != null && api.getOwnerId().equals(userId));

            if (isOwner) {
                log.info("User {} is the owner of API {}, skipping subscription check", userId, dto.getApiId());
            } else {
                if (dto.getApiKey() == null || dto.getApiKey().isBlank()) {
                    log.warn("Access denied: API key required for non-owner access to API {}", dto.getApiId());
                    return new SandboxResponseDTO(403, null, 0L, 0, errorMessages.getApiKeyRequired());
                }

                try {
                    SubscriptionLimitsDTO limits = subscriptionServiceClient.getSubscriptionLimits(dto.getApiKey());

                    if (!"ACTIVE".equals(limits.getStatus())) {
                        String errorMsg = "CANCELLED".equals(limits.getStatus()) ? errorMessages.getSubscriptionCancelled() : errorMessages.getSubscriptionExpired();
                        log.warn("Access denied: Subscription status is {} for apiKey={}", limits.getStatus(), dto.getApiKey());
                        return new SandboxResponseDTO(403, null, 0L, 0, errorMsg);
                    }

                    if (limits.getEndDate() != null && limits.getEndDate().isBefore(java.time.LocalDate.now())) {
                        log.warn("Access denied: Subscription expired on {} for apiKey={}", limits.getEndDate(), dto.getApiKey());
                        return new SandboxResponseDTO(403, null, 0L, 0, errorMessages.getSubscriptionExpired());
                    }

                    if (!limits.getApiId().equals(dto.getApiId())) {
                        log.warn("Access denied: API key {} is for API {}, not {}", dto.getApiKey(), limits.getApiId(), dto.getApiId());
                        return new SandboxResponseDTO(403, null, 0L, 0, errorMessages.getInvalidApiKeyForApi());
                    }

                if (limits.getRequestLimit() != null && limits.getRequestLimit() != -1) {
                    long current = limits.getCurrentUsage() != null ? limits.getCurrentUsage() : 0L;
                    if (current >= limits.getRequestLimit()) {
                        log.warn("Access denied: Request limit exceeded for apiKey={}", dto.getApiKey());
                        return new SandboxResponseDTO(429, null, 0L, 0, "Request limit exceeded");
                    }
                }

                } catch (Exception e) {
                    log.error("Failed to validate subscription for apiKey={}", dto.getApiKey(), e);
                    return new SandboxResponseDTO(403, null, 0L, 0, errorMessages.getInvalidOrExpiredApiKey());
                }
            }

            log.info("Fetching execution data from API Catalog for apiId={}, endpointId={}", dto.getApiId(), dto.getEndpointId());
            ApiExecutionDataDTO apiData = apiCatalogClient.getExecutionData(dto.getApiId(), dto.getEndpointId());
            if (apiData == null) {
                log.error("API Catalog returned null for apiId={}, endpointId={}", dto.getApiId(), dto.getEndpointId());
                return new SandboxResponseDTO(500, null, 0L, 0, errorMessages.getApiDataNotFound());
            }

            if (apiData.getBaseUrl() == null || apiData.getBaseUrl().isBlank()) {
                log.error("API Base URL is missing for apiId={}, endpointId={}", dto.getApiId(), dto.getEndpointId());
                return new SandboxResponseDTO(500, null, 0L, 0, errorMessages.getApiBaseUrlMissing());
            }

            endpoint = apiData.getEndpoint() != null ? apiData.getEndpoint() : "/";
            method = apiData.getMethod() != null ? apiData.getMethod().toUpperCase() : "GET";
            url = buildUrl(apiData.getBaseUrl(), endpoint, apiData.getParamsJson());
            log.info("FINAL URL = {}", url);

            HttpHeaders headers = buildHeaders(apiData.getHeadersJson());
            log.info("HEADERS = {}", headers);

            String requestBody = apiData.getSampleRequest();
            log.info("BODY = {}", requestBody);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            SandboxRequest request = null;
            Integer requestId = null;
            try {
                request = saveRequest(dto, url, method, headers, requestBody);
                requestId = request.getId();
                log.info("Request saved with ID: {}", requestId);
            } catch (Exception e) {
                log.error("Failed to save initial request to DB, continuing without persistence", e);
            }
            long start = System.currentTimeMillis();

            try {
                log.info("Executing external API call: {} {}", method, url);
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.valueOf(method), entity, String.class);

                latency = System.currentTimeMillis() - start;
                responseBody = response.getBody();
                statusCode = response.getStatusCode().value();
                log.info("API call successful: status={}", statusCode);
                saveResponse(requestId, statusCode, responseBody, latency, null);
                logUsage(dto, endpoint, method, statusCode, latency);
                return new SandboxResponseDTO(statusCode, responseBody, latency, responseBody != null ? responseBody.length() : 0, null);

            } catch (HttpStatusCodeException e) {
                latency = System.currentTimeMillis() - start;
                statusCode = e.getStatusCode().value();
                responseBody = e.getResponseBodyAsString();
                log.warn("API call returned error status: {}", statusCode);
                saveResponse(requestId, statusCode, responseBody, latency, e.getMessage());
                logUsage(dto, endpoint, method, statusCode, latency);
                return new SandboxResponseDTO(statusCode, responseBody, latency, 0, e.getMessage());

            } catch (Exception e) {
                latency = System.currentTimeMillis() - start;
                log.error("API call failed with exception: {}", e.getMessage(), e);
                saveResponse(requestId, 500, null, latency, e.getMessage());
                logUsage(dto, endpoint, method, 500, latency);
                return new SandboxResponseDTO(500, null, latency, 0, errorMessages.getExecutionFailed().replace("{0}", e.getMessage()));
            }

        } catch (Exception e) {
            log.error("General execution failure: {}", e.getMessage(), e);
            return new SandboxResponseDTO(500, null, 0L, 0, errorMessages.getInternalError().replace("{0}", e.getMessage()));
        }
    }

    private void saveResponse(Integer requestId, int statusCode, String body, long latency, String error) {
        if (requestId == null) {
            log.warn("Cannot save response: requestId is null (request was not persisted)");
            return;
        }
        log.info("Attempting to save response to DB for requestId={}, status={}", requestId, statusCode);
        try {
            SandboxResponse res = SandboxResponse.builder().requestId(requestId).statusCode(statusCode).responseBody(body).latencyMs(latency).responseSize(body != null ? body.length() : 0).errorMessage(error).build();
            responseRepo.save(res);
            log.info("Successfully saved response to DB for requestId={}", requestId);
        } catch (Exception e) {
            log.error("Failed to save response to DB for requestId={}", requestId, e);
        }
    }

    private SandboxRequest saveRequest(SandboxRequestDTO dto, String url, String method, HttpHeaders headers, String body) {
        try {
            int finalUserId = (dto.getUserId() != null) ? dto.getUserId() : -1;
            log.info("Saving request to DB with userId: {}", finalUserId);

            SandboxRequest request = SandboxRequest.builder().apiId(dto.getApiId()).endpointId(dto.getEndpointId()).userId(finalUserId).method(method).fullUrl(url).headersJson(objectMapper.writeValueAsString(headers.toSingleValueMap())).queryParamsJson("{}").pathVariablesJson("{}").requestBody(body).build();
            SandboxRequest saved = requestRepo.save(request);
            requestRepo.flush();
            return saved;

        } catch (Exception e) {
            log.error("DB SAVE FAILED", e);
            throw new RuntimeException(e);
        }
    }

    private String buildUrl(String baseUrl, String endpoint, String paramsJson) {
        String url = baseUrl + endpoint;

        try {
            if (paramsJson == null || paramsJson.isBlank()) return url;

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            try {
                Map<String, String> params = objectMapper.readValue(paramsJson, new TypeReference<>() {});
                params.forEach(builder::queryParam);
            } catch (Exception e) {
                List<Map<String, String>> list = objectMapper.readValue(paramsJson, new TypeReference<>() {});
                for (Map<String, String> m : list) {
                    if (m.containsKey("name")) {
                        builder.queryParam(m.get("name"), "");
                    }
                }
            }
            return builder.toUriString();
        } catch (Exception e) {
            log.warn("Params parsing failed: {}", e.getMessage());
            return url;
        }
    }

    private HttpHeaders buildHeaders(String headersJson) {
        HttpHeaders headers = new HttpHeaders();

        try {
            if (headersJson == null || headersJson.isBlank()) return headers;
            try {
                Map<String, String> map = objectMapper.readValue(headersJson, new TypeReference<>() {});
                map.forEach(headers::set);
            } catch (Exception e) {
                List<Map<String, String>> list = objectMapper.readValue(headersJson, new TypeReference<>() {});
                for (Map<String, String> m : list) {
                    if (m.containsKey("name")) {
                        headers.set(m.get("name"), "");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Header parsing failed: {}", e.getMessage());
        }
        return headers;
    }

    private void logUsage(SandboxRequestDTO dto, String endpoint, String method, int status, long latency) {
        if (dto.getApiKey() == null || dto.getApiKey().isBlank()) {
            log.warn("Usage not logged: apiKey missing for user {} on API {}", dto.getUserId(), dto.getApiId());
            return;
        }
        try {
            RecordUsageRequestDTO usage = RecordUsageRequestDTO.builder().apiKey(dto.getApiKey()).endpoint(endpoint).statusCode(status).responseTimeMs(latency).httpMethod(method).ipAddress(null).build();
            log.info("Recording usage: apiKey={}, endpoint={}, status={}, latency={}ms", dto.getApiKey(), endpoint, status, latency);
            var response = subscriptionServiceClient.recordUsage(usage);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Usage recorded successfully for apiKey={}, status={}", dto.getApiKey(), response.getStatusCode());
            } else {
                log.error("Usage recording failed for apiKey={}, status={}, body={}", dto.getApiKey(), response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("Usage logging failed for apiKey={}: {}", dto.getApiKey(), e.getMessage(), e);
        }
    }
}