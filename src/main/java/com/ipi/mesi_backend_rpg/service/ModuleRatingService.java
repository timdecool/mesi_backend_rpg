package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.AggregatedRatingsDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleCommentDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleRatingDTO;
import com.ipi.mesi_backend_rpg.mapper.ModuleCommentMapper;
import com.ipi.mesi_backend_rpg.mapper.ModuleRatingMapper;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleComment;
import com.ipi.mesi_backend_rpg.model.ModuleRating;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.repository.ModuleCommentRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleRatingService {

    private final ModuleRatingRepository moduleRatingRepository;
    private final ModuleCommentMapper moduleCommentMapper;
    private final ModuleRatingMapper moduleRatingMapper;

    public AggregatedRatingsDTO createRating(ModuleRatingDTO moduleRatingDTO) {
        ModuleRating moduleRating = moduleRatingMapper.toEntity(moduleRatingDTO);
        ModuleRating saved = moduleRatingRepository.save(moduleRating);
        return moduleRatingMapper.toAggregated(moduleRatingMapper.toDTO(saved));
    }

    public AggregatedRatingsDTO updateRating(ModuleRatingDTO moduleRatingDTO, Long id) {
        ModuleRating rating = moduleRatingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "comment not found"));
        ModuleRating newRating = moduleRatingMapper.toEntity(moduleRatingDTO);

        newRating.setId(rating.getId());
        newRating.setCreatedAt(rating.getCreatedAt());
        newRating.setModule(rating.getModule());
        newRating.setModuleVersion(rating.getModuleVersion());
        ModuleRating saved = moduleRatingRepository.save(newRating);
        return moduleRatingMapper.toAggregated(moduleRatingMapper.toDTO(saved));
    }

    public void deleteRating(Long id) {
        ModuleRating rating = moduleRatingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "module version not found"));

        moduleRatingRepository.delete(rating);
    }

    public AggregatedRatingsDTO findAggregatedRatingsByModule(Module module) {
        if (module == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "module not found");
        }

        return new AggregatedRatingsDTO(
                moduleRatingRepository.countByModuleId(module.getId()),
                0,
                moduleRatingRepository.findAverageRatingByModuleId(module.getId()),
                0f,
                null
        );
    }

    public AggregatedRatingsDTO findAggregatedRatingsByModuleVersion(ModuleVersion moduleVersion) {
        if (moduleVersion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "module version not found");
        }

        return new AggregatedRatingsDTO(
                moduleRatingRepository.countByModuleId(moduleVersion.getModule().getId()),
                moduleRatingRepository.countByModuleVersionId(moduleVersion.getId()),
                moduleRatingRepository.findAverageRatingByModuleId(moduleVersion.getModule().getId()),
                moduleRatingRepository.findAverageRatingByModuleVersionId(moduleVersion.getId()),
                null
        );
    }

}
