package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.TagDTO;
import com.ipi.mesi_backend_rpg.model.Tag;
import org.springframework.stereotype.Service;

@Service
public class TagMapperService {

    public TagDTO toDTO(Tag tag) {
        return new TagDTO(
                tag.getId(),
                tag.getName(),
                tag.getModules()
        );
    }

    public Tag toEntity(TagDTO tagDTO) {
        Tag tag = new Tag();
        tag.setName(tagDTO.name());
        tag.setModules(tagDTO.modules());
        return tag;
    }
}
