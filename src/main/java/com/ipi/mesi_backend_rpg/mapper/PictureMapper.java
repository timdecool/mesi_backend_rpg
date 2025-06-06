package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.PictureDTO;
import com.ipi.mesi_backend_rpg.model.Picture;
import com.ipi.mesi_backend_rpg.repository.PictureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PictureMapper {

    private final PictureRepository pictureRepository;

    public PictureDTO toDTO(Picture picture) {
        return new PictureDTO(
                picture.getId(),
                picture.getTitle(),
                picture.getSrc(),
                picture.getCreatedAt(),
                picture.getUpdatedAt()
        );
    }

    public Picture toEntity(PictureDTO dto) {
        if (dto.id() != null) {
            return pictureRepository.findById(dto.id())
                    .orElse(new Picture(dto.title(), dto.src()));
        }
        return new Picture(dto.title(), dto.src());
    }

}
