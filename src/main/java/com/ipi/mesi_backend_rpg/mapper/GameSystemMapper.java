package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.GameSystemDTO;
import com.ipi.mesi_backend_rpg.model.GameSystem;
import org.springframework.stereotype.Service;

@Service
public class GameSystemMapper {

    public GameSystemDTO toDTO(GameSystem gameSystem) {
        return new GameSystemDTO(
                gameSystem.getId(),
                gameSystem.getName(),
                gameSystem.getCreatedAt(),
                gameSystem.getUpdatedAt()
        );
    }

    public GameSystem toEntity(GameSystemDTO gameSystemDTO) {
        return new GameSystem(
                gameSystemDTO.name(),
                gameSystemDTO.createdAt(),
                gameSystemDTO.updatedAt()
        );
    }
}
