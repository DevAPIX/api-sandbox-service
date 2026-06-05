package com.devapix.sandbox_service.client;


import com.devapix.sandbox_service.dto.RecordUsageRequestDTO;
import com.devapix.sandbox_service.dto.SubscriptionLimitsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "API-SUBSCRIPTION-SERVICE")
public interface SubscriptionServiceClient {

    @PostMapping("/internal/usage")
    org.springframework.http.ResponseEntity<String> recordUsage(@RequestBody RecordUsageRequestDTO request);

    @GetMapping("/internal/subscriptions/active")
    boolean hasActiveSubscription(@RequestParam("consumerId") Integer consumerId, @RequestParam("apiId") Integer apiId);

    @GetMapping("/subscriptions/internal/{apiKey}/limits")
    SubscriptionLimitsDTO getSubscriptionLimits(@PathVariable("apiKey") String apiKey);
}
