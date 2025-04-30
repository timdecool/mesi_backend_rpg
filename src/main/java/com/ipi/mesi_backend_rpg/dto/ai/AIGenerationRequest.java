package com.ipi.mesi_backend_rpg.dto.ai;

import java.util.Map;

import com.ipi.mesi_backend_rpg.enums.EBlockType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AIGenerationRequest {
    private EBlockType type;
    private Map<String, String> parameters;
}
