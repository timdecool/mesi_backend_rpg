package com.ipi.mesi_backend_rpg.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatBlockDTO extends BlockDTO {
    private String statRules;
    private String statValues;
    public StatBlockDTO() {}

    public StatBlockDTO(Long id, Long moduleVersionId, String title, Integer blockOrder, UserDTO creator, String statRules, String statValues) {
        super(id, moduleVersionId, title, blockOrder, creator);
        this.statRules = statRules;
        this.statValues = statValues;
    }
}
