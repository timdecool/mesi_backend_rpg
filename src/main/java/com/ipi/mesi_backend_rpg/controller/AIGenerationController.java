package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.ai.AnthropicDTOs.AIGenerationRequest;
import com.ipi.mesi_backend_rpg.dto.ai.AnthropicDTOs.AIGenerationResponse;
import com.ipi.mesi_backend_rpg.service.AIBlockGenerationService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/generate")
    public ResponseEntity<AIGenerationResponse> generateContent(@RequestBody AIGenerationRequest request) {
        String content = this.aiBlockGenerationService.generateBlock(request.getType(), request.getParameters());
        AIGenerationResponse response = new AIGenerationResponse(content, request.getType());
        return ResponseEntity.ok(response);
    }
}