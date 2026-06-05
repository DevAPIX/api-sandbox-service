package com.devapix.sandbox_service.dto;
import lombok.Data;

@Data
public class ApiCatalogResponse {
    private Integer id;
    private Integer ownerId;
    private String name;
    private String baseUrl;
    private String visibility;
}
