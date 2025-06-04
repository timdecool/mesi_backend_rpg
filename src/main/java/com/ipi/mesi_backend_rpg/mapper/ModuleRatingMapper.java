package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.AggregatedRatingsDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleCommentDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleRatingDTO;
import com.ipi.mesi_backend_rpg.model.ModuleComment;
import com.ipi.mesi_backend_rpg.model.ModuleRating;
import com.ipi.mesi_backend_rpg.repository.ModuleRatingRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleVersionRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModuleRatingMapper {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ModuleRepository moduleRepository;
    private final ModuleVersionRepository moduleVersionRepository;
    private final ModuleRatingRepository moduleRatingRepository;

    public ModuleRatingDTO toDTO(ModuleRating moduleRating) {
        return new ModuleRatingDTO(
                moduleRating.getId(),
                moduleRating.getModule().getId(),
                moduleRating.getModuleVersion().getId(),
                userMapper.toDTO(moduleRating.getUser()),
                moduleRating.getRating(),
                moduleRating.getCreatedAt(),
                moduleRating.getUpdatedAt()
        );
    }

    public ModuleRating toEntity(ModuleRatingDTO moduleRatingDTO) {
        ModuleRating moduleRating = new ModuleRating();
        moduleRating.setId(moduleRatingDTO.id());
        moduleRating.setModule(moduleRepository.findById(moduleRatingDTO.moduleId())
                .orElseThrow(() -> new IllegalArgumentException("Module not found")));
        moduleRating.setModuleVersion(moduleVersionRepository.findById(moduleRatingDTO.moduleVersionId())
                .orElseThrow(() -> new IllegalArgumentException("ModuleVersion not found")));
        moduleRating.setRating(moduleRatingDTO.rating());
        return moduleRating;
    }

    public AggregatedRatingsDTO toAggregated(ModuleRatingDTO moduleRatingDTO) {

        int moduleVersionNumberOfRatings = 0;
        Float moduleVersionAverageRating = 0f;
        if(moduleRatingDTO.moduleVersionId() != null) {
            moduleVersionNumberOfRatings = moduleRatingRepository.countByModuleVersionId(moduleRatingDTO.moduleVersionId());
            moduleVersionAverageRating = moduleRatingRepository.findAverageRatingByModuleVersionId(moduleRatingDTO.moduleVersionId());
        }

        return new AggregatedRatingsDTO(
                moduleRatingRepository.countByModuleId(moduleRatingDTO.moduleId()),
                moduleVersionNumberOfRatings,
                moduleRatingRepository.findAverageRatingByModuleId(moduleRatingDTO.moduleId()),
                moduleVersionAverageRating,
                moduleRatingDTO
        );
    }
}
