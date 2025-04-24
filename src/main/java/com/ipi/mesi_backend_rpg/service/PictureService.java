package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.PictureDTO;
import com.ipi.mesi_backend_rpg.mapper.PictureMapper;
// import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.Picture;
// import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import com.ipi.mesi_backend_rpg.repository.PictureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PictureService {

    private final PictureRepository pictureRepository;
    private final PictureMapper pictureMapper;
    // private final ModuleRepository moduleRepository;
    
    public PictureDTO createModulePicture(PictureDTO pictureDTO, Long moduleId) {
        Picture picture = pictureMapper.toEntity(pictureDTO);
        pictureRepository.save(picture);

        return pictureMapper.toDTO(picture);
    }
    // TODO: Ajouter les services pour assoccier de création d'images à des utilisation PictureBlock et UserProfile

    public PictureDTO createBlockPicture(PictureDTO pictureDTO, Long blockId) {
        Picture picture = pictureMapper.toEntity(pictureDTO);
        pictureRepository.save(picture);

        return pictureMapper.toDTO(picture);
    }

//    public List<PictureDTO> getPictures(Long pictureUsageId, PictureUsage pictureUsage) {
//        List<Picture> pictures = pictureRepository.findByPictureUsageAndPictureUsageId(pictureUsage, pictureUsageId);
//        return pictures.stream()
//                .map(pictureMapper::toDTO)
//                .toList();
//    }

    public PictureDTO updatePicture(PictureDTO pictureDTO, Long id) {
        Picture existingPicture = pictureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Picture not found"));

        existingPicture.setTitle(pictureDTO.title());
        existingPicture.setSrc(pictureDTO.src());

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
