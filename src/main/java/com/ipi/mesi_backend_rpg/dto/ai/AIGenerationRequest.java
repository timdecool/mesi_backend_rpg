package com.ipi.mesi_backend_rpg.dto.ai;

import java.util.Map;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AIGenerationRequest {
    private String type;
    private Map<String, String> parameters;
}
