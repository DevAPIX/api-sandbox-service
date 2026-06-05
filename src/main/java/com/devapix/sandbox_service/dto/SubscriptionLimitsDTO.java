package com.devapix.sandbox_service.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class SubscriptionLimitsDTO {

    private Integer id;
    private Integer apiId;
    private Integer planId;
    private Integer consumerId;
    private String status;
    private String apiKey;
    private LocalDate startDate;
    private LocalDate endDate;
    private String planName;
    private Integer requestLimit;
    private Long currentUsage;
}
