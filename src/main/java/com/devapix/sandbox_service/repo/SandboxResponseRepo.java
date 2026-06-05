package com.devapix.sandbox_service.repo;

import com.devapix.sandbox_service.model.SandboxResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SandboxResponseRepo extends JpaRepository<SandboxResponse, Integer> {
}
