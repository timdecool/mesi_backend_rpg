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
        String content;
        
        switch (request.getType().toLowerCase()) {
            case "paragraph":
                content = aiBlockGenerationService.generateParagraphBlock(request.getParameters());
                break;
            case "stat":
                content = aiBlockGenerationService.generateStatBlock(request.getParameters());
                break;
            case "music":
                content = aiBlockGenerationService.generateMusicDescription(request.getParameters());
                break;
            default:
                return ResponseEntity.badRequest().build();
        }
        
        AIGenerationResponse response = new AIGenerationResponse(content, request.getType());
        return ResponseEntity.ok(response);
    }
}