package com.devapix.sandbox_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sandbox.errors")
public class ErrorMessages {

    private String apiNotFound;
    private String apiDataNotFound;
    private String apiBaseUrlMissing;
    private String noSubscription;
    private String executionFailed;
    private String internalError;
    private String apiKeyRequired;
    private String subscriptionCancelled;
    private String subscriptionExpired;
    private String invalidApiKeyForApi;
    private String invalidOrExpiredApiKey;
    private String tokenMissing;
}
