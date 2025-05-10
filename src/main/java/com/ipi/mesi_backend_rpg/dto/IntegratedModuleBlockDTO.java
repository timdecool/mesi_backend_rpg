package com.ipi.mesi_backend_rpg.dto;

public class IntegratedModuleBlockDTO extends BlockDTO {

    private Long moduleId;

    public IntegratedModuleBlockDTO() {}

    public IntegratedModuleBlockDTO(Long moduleId, Long id, Long moduleVersionId, String title, Integer blockOrder, UserDTO creator) {
        super(id, moduleVersionId, title, blockOrder, creator);
        this.moduleId = moduleId;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }
}
