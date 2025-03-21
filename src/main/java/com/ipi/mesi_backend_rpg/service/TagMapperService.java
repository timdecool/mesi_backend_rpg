package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.TagDTO;
import com.ipi.mesi_backend_rpg.model.Tag;
import org.springframework.stereotype.Service;

@Service
public class TagMapperService {

    public TagDTO mapTagToTagDTO(Tag tag) {
        return new TagDTO(
                tag.getId(),
                tag.getName(),
                tag.getModules()
        );
    }

}
