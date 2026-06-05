package com.devapix.sandbox_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "sandbox_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SandboxRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer apiId;

    private int userId;

    @Column(nullable = false)
    private Integer endpointId;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String fullUrl;

    @Column(columnDefinition = "TEXT")
    private String headersJson;

    @Column(columnDefinition = "TEXT")
    private String queryParamsJson;

    @Column(columnDefinition = "TEXT")
    private String pathVariablesJson;

    @Column(columnDefinition = "TEXT")
    private String requestBody;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
