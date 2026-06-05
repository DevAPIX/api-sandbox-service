package com.devapix.sandbox_service.controller;
import com.devapix.sandbox_service.dto.SandboxRequestDTO;
import com.devapix.sandbox_service.dto.SandboxResponseDTO;
import com.devapix.sandbox_service.service.SandboxService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SandboxController {

    private final SandboxService sandboxService;

    @PostMapping("/execute")
    public ResponseEntity<SandboxResponseDTO> execute(@RequestParam Integer apiId, @RequestParam Integer endpointId,@RequestHeader(value = "X-Api-Key", required = false) String apiKey, @Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdStr) {
        Integer userId = Integer.parseInt(userIdStr);
        SandboxRequestDTO dto = new SandboxRequestDTO();
        dto.setApiId(apiId);
        dto.setEndpointId(endpointId);
        dto.setApiKey(apiKey);
        dto.setUserId(userId);
        log.info("POST /execute → apiId={}, endpointId={}, userId={}, apiKey={}", apiId, endpointId, userId, apiKey != null ? "present" : "missing");
        return ResponseEntity.ok(sandboxService.execute(dto));
    }
}