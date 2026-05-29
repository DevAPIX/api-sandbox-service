package com.devapix.sandbox_service.mapper;
import com.devapix.sandbox_service.dto.SandboxResponseDTO;
import com.devapix.sandbox_service.model.SandboxResponse;

public class SandboxMapper {

    public static SandboxResponseDTO toDTO(SandboxResponse entity) {
        return new SandboxResponseDTO(entity.getStatusCode(), entity.getResponseBody(),
                entity.getLatencyMs(), entity.getResponseSize(), entity.getErrorMessage());
    }
}