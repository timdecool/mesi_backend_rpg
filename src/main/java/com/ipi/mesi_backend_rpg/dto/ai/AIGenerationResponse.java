package com.ipi.mesi_backend_rpg.dto.ai;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AIGenerationResponse {
    private String content;
    private String type;
}
