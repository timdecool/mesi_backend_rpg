package com.ipi.mesi_backend_rpg.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MusicBlockDTO extends BlockDTO {

    private String label;
    private String src;

    public MusicBlockDTO() {
    }

    public MusicBlockDTO(String label, String src, Long id, Long moduleVersionId, String title, Integer blockOrder, UserDTO creator) {
        super(id, moduleVersionId, title, blockOrder, creator);
        this.label = label;
        this.src = src;
    }


}
