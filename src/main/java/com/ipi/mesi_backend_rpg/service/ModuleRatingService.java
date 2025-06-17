package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.AggregatedRatingsDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleRatingDTO;
import com.ipi.mesi_backend_rpg.mapper.ModuleRatingMapper;
import com.ipi.mesi_backend_rpg.model.*;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.repository.ModuleRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class ModuleRatingService {

    private final ModuleRatingRepository moduleRatingRepository;
    private final ModuleRatingMapper moduleRatingMapper;
    private final UserService userService;

    public AggregatedRatingsDTO createRating(ModuleRatingDTO moduleRatingDTO) {

        ModuleRating moduleRating = moduleRatingMapper.toEntity(moduleRatingDTO);
        if(moduleRating.getRating() < 0 || moduleRating.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "rating is out of range");
        }

        User user = userService.getAuthenticatedUser();
        if(moduleRatingRepository.findModuleRatingByModuleVersionIdAndUserId(moduleRating.getModuleVersion().getId(), user.getId()) != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not authorized");
        }

        moduleRating.setUser(user);
        ModuleRating saved = moduleRatingRepository.save(moduleRating);
        return moduleRatingMapper.toAggregated(moduleRatingMapper.toDTO(saved));
    }

    public AggregatedRatingsDTO updateRating(ModuleRatingDTO moduleRatingDTO, Long id) {
        ModuleRating rating = moduleRatingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "comment not found"));

        if(!rating.getUser().getId().equals(userService.getAuthenticatedUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not authorized");
        }

        ModuleRating newRating = moduleRatingMapper.toEntity(moduleRatingDTO);
        if(newRating.getRating() < 0 || newRating.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "rating is out of range");
        }

        newRating.setId(rating.getId());
        newRating.setCreatedAt(rating.getCreatedAt());
        newRating.setModule(rating.getModule());
        newRating.setModuleVersion(rating.getModuleVersion());
        newRating.setUser(rating.getUser());

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

        User user = userService.getAuthenticatedUserOrNull();
        ModuleRatingDTO moduleRatingDTO = null;
        
        if (user != null) {
            ModuleRating moduleRating = moduleRatingRepository.findModuleRatingByModuleIdAndUserId(module.getId(), user.getId());
            if (moduleRating != null) {
                moduleRatingDTO = moduleRatingMapper.toDTO(moduleRating);
            }
        }

        return new AggregatedRatingsDTO(
                moduleRatingRepository.countByModuleId(module.getId()),
                0,
                moduleRatingRepository.findAverageRatingByModuleId(module.getId()),
                0f,
                moduleRatingDTO
        );
    }

    public AggregatedRatingsDTO findAggregatedRatingsByModuleVersion(ModuleVersion moduleVersion) {
        if (moduleVersion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "module version not found");
        }

        User user = userService.getAuthenticatedUserOrNull();
        ModuleRatingDTO moduleRatingDTO = null;
        
        if (user != null) {
            ModuleRating moduleRating = moduleRatingRepository.findModuleRatingByModuleVersionIdAndUserId(moduleVersion.getId(), user.getId());
            if (moduleRating != null) {
                moduleRatingDTO = moduleRatingMapper.toDTO(moduleRating);
            }
        }

        return new AggregatedRatingsDTO(
                moduleRatingRepository.countByModuleId(moduleVersion.getModule().getId()),
                moduleRatingRepository.countByModuleVersionId(moduleVersion.getId()),
                moduleRatingRepository.findAverageRatingByModuleId(moduleVersion.getModule().getId()),
                moduleRatingRepository.findAverageRatingByModuleVersionId(moduleVersion.getId()),
                moduleRatingDTO
        );
    }

}
