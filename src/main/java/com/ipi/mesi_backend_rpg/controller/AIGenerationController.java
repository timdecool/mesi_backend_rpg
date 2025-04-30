package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.ai.AIGenerationRequest;
import com.ipi.mesi_backend_rpg.dto.ai.AIGenerationResponse;
import com.ipi.mesi_backend_rpg.service.AIBlockGenerationService;
import com.ipi.mesi_backend_rpg.service.ModuleGenerationService;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIGenerationController {

    private final AIBlockGenerationService aiBlockGenerationService;
    private final ModuleGenerationService moduleGenerationService;

    @PostMapping("/generate")
    public ResponseEntity<AIGenerationResponse> generateContent(@RequestBody AIGenerationRequest request) {
        String content = this.aiBlockGenerationService.generateBlock(request.getType(), request.getParameters());
        AIGenerationResponse response = new AIGenerationResponse(content, request.getType());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-module")
    public ResponseEntity<Map<String, Object>> generateModule(@RequestBody Map<String, Object> request) {
        String theme = (String) request.getOrDefault("theme", "");
        String title = (String) request.getOrDefault("title", "");
        String description = (String) request.getOrDefault("description", "");
        Long gameSystemId = Long.valueOf(request.getOrDefault("gameSystemId", "1").toString());
        Long userId = Long.valueOf(request.getOrDefault("userId", "1").toString());
        
        Map<String, Object> response = moduleGenerationService.generateCompleteModule(theme, title, description, gameSystemId, userId);
        return ResponseEntity.ok(response);
    }
}