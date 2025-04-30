package com.ipi.mesi_backend_rpg.dto.ai;

import com.ipi.mesi_backend_rpg.enums.EBlockType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AIGenerationResponse {
    private String content;
    private EBlockType type;
}
