package com.ipi.mesi_backend_rpg.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ipi.mesi_backend_rpg.dto.ai.AIGenerationRequest;
import com.ipi.mesi_backend_rpg.dto.ai.AIGenerationResponse;
import com.ipi.mesi_backend_rpg.service.AIBlockGenerationService;
import com.ipi.mesi_backend_rpg.service.ModuleGenerationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIGenerationController {

    private final AIBlockGenerationService aiBlockGenerationService;
    private final ModuleGenerationService moduleGenerationService;

    @PostMapping("/generate")
    public ResponseEntity<AIGenerationResponse> generateContent(@RequestBody AIGenerationRequest request) {
        AIGenerationResponse response = aiBlockGenerationService.generateBlock(request.getType(),
                request.getParameters());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-module")
    public ResponseEntity<Map<String, Object>> generateModule(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = moduleGenerationService.generateCompleteModule(request);
        return ResponseEntity.ok(response);
    }
}