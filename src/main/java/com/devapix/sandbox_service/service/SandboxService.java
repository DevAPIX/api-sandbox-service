package com.devapix.sandbox_service.service;

import com.devapix.sandbox_service.dto.SandboxRequestDTO;
import com.devapix.sandbox_service.dto.SandboxResponseDTO;

 public interface SandboxService {
        SandboxResponseDTO execute(SandboxRequestDTO dto);
    }

