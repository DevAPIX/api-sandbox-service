package com.devapix.sandbox_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds sandbox.errors.* from sandbox-service.yml (via config-server)
 * into a type-safe bean — no hardcoded strings in service code.
 */
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
}
