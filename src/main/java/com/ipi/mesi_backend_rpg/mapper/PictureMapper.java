package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.PictureDTO;
import com.ipi.mesi_backend_rpg.model.Picture;
import org.springframework.stereotype.Service;

@Service
public class PictureMapper {

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
        return new Picture(
                dto.title(),
                dto.src()
        );
    }

}
