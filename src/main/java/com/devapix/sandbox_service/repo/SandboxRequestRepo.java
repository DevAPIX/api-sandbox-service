package com.devapix.sandbox_service.repo;
import com.devapix.sandbox_service.model.SandboxRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SandboxRequestRepo extends JpaRepository<SandboxRequest, Integer> {
}
