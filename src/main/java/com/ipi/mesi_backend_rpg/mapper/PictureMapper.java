package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.PictureDTO;
import com.ipi.mesi_backend_rpg.model.Picture;
import org.springframework.stereotype.Service;

@Service
public class PictureMapper {

    public PictureDTO toDTO(Picture picture) {
        return new PictureDTO(
                picture.getId(),
                picture.getPictureUsage(),
                picture.getPictureUsageId(),
                picture.getTitle(),
                picture.getSrc(),
                picture.getCreatedAt(),
                picture.getUpdateAt()
        );
    }

    public Picture toEntity(PictureDTO dto) {
        Picture picture = new Picture(
                dto.pictureUsage(),
                dto.pictureUsageId(),
                dto.title(),
                dto.src(),
                dto.createdAt(),
                dto.updateAt()
        );
        picture.setId(dto.id());
        return picture;
    }

}
