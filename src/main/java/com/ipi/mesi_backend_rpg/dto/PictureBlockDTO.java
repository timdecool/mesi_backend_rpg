package com.ipi.mesi_backend_rpg.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PictureBlockDTO extends BlockDTO {

    private String label;

    private PictureDTO picture;

    public PictureBlockDTO() {
        setType("picture");
    }

    public PictureBlockDTO(String label, PictureDTO picture, Long id, Long moduleVersionId, String title, Integer blockOrder, UserDTO creator) {
        super(id, moduleVersionId, title, blockOrder, creator);
        this.label = label;
        this.picture = picture;
        setType("picture");
    }


}
