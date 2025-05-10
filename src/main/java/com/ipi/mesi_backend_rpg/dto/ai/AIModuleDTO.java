package com.ipi.mesi_backend_rpg.dto.ai;

import java.util.List;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleResponseDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AIModuleDTO {
    private ModuleResponseDTO module;
    private ModuleVersionDTO moduleVersion;
    private List<BlockDTO> blocks;
}