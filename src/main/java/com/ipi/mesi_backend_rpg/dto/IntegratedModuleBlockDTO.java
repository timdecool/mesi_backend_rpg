package com.ipi.mesi_backend_rpg.dto;

public class IntegratedModuleBlockDTO extends BlockDTO {

    private Long moduleId;

    public IntegratedModuleBlockDTO() {
    }

    public IntegratedModuleBlockDTO(Long moduleId, Long id, Long moduleVersionId, String title, Integer blockOrder, String createdBy) {
        super(id, moduleVersionId, title, blockOrder, createdBy);
        this.moduleId = moduleId;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }
}
