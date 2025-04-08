package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.PictureDTO;
import com.ipi.mesi_backend_rpg.mapper.PictureMapper;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.Picture;
import com.ipi.mesi_backend_rpg.model.PictureUsage;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import com.ipi.mesi_backend_rpg.repository.PictureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class PictureService {

    private final PictureRepository pictureRepository;
    private final PictureMapper pictureMapper;
    private final ModuleRepository moduleRepository;

    public PictureService(PictureRepository pictureRepository, PictureMapper pictureMapper, ModuleRepository moduleRepository) {
        this.pictureRepository = pictureRepository;
        this.pictureMapper = pictureMapper;
        this.moduleRepository = moduleRepository;
    }

    public PictureDTO createModulePicture(PictureDTO pictureDTO, Long moduleId) {
        Module module = moduleRepository.findById(moduleId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));
        Picture picture = pictureMapper.toEntity(pictureDTO);
        picture.setPictureUsage(PictureUsage.MODULE);
        picture.setPictureUsageId(moduleId);
        picture.setCreatedAt(LocalDate.now());
        picture.setUpdateAt(LocalDate.now());
        pictureRepository.save(picture);

        return pictureMapper.toDTO(picture);
    }
    // TODO: Ajouter les services pour assoccier de création d'images à des utilisation PictureBlock et UserProfile

    public List<PictureDTO> getPictures(Long pictureUsageId, PictureUsage pictureUsage) {
        List<Picture> pictures = pictureRepository.findByPictureUsageAndPictureUsageId(pictureUsage, pictureUsageId);
        return pictures.stream()
                .map(pictureMapper::toDTO)
                .toList();
    }

    public PictureDTO updatePicture(PictureDTO pictureDTO, Long id) {
        Picture existingPicture = pictureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Picture not found"));

        existingPicture.setTitle(pictureDTO.title());
        existingPicture.setSrc(pictureDTO.src());
        existingPicture.setUpdateAt(LocalDate.now());

        pictureRepository.save(existingPicture);
        return pictureMapper.toDTO(existingPicture);
    }

    public PictureDTO deletePicture(Long id) {
        Picture picture = pictureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Picture not found"));
        pictureRepository.delete(picture);

        return pictureMapper.toDTO(picture);
    }

}
