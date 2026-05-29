package com.devapix.sandbox_service.client;

import com.devapix.sandbox_service.dto.RecordUsageRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "subscription-service", url = "${services.subscription.base-url}")
public interface SubscriptionServiceClient {

    @PostMapping("/internal/usage")
    void recordUsage(@RequestBody RecordUsageRequestDTO request);

    @org.springframework.web.bind.annotation.GetMapping("/internal/subscriptions/active")
    boolean hasActiveSubscription(@org.springframework.web.bind.annotation.RequestParam("consumerId") Integer consumerId, @org.springframework.web.bind.annotation.RequestParam("apiId") Integer apiId);
}
