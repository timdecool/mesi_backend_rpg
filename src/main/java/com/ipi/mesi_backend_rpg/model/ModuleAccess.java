package com.ipi.mesi_backend_rpg.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ModuleAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference("module_module_access")
    private Module module;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference("user_module_access")
    private User user;

    private boolean canView;
    private boolean canEdit;
    private boolean canPublish;
    private boolean canInvite;

    public ModuleAccess(Module module, User user, boolean canView, boolean canEdit, boolean canPublish, boolean canInvite) {
        this.module = module;
        this.user = user;
        this.canView = canView;
        this.canEdit = canEdit;
        this.canPublish = canPublish;
        this.canInvite = canInvite;
    }
}
