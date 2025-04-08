package com.ipi.mesi_backend_rpg.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
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

    public ModuleAccess() {
    }

    public ModuleAccess(Module module, User user, boolean canView, boolean canEdit, boolean canPublish, boolean canInvite) {
        this.module = module;
        this.user = user;
        this.canView = canView;
        this.canEdit = canEdit;
        this.canPublish = canPublish;
        this.canInvite = canInvite;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isCanView() {
        return canView;
    }

    public void setCanView(boolean canView) {
        this.canView = canView;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean isCanPublish() {
        return canPublish;
    }

    public void setCanPublish(boolean canPublish) {
        this.canPublish = canPublish;
    }

    public boolean isCanInvite() {
        return canInvite;
    }

    public void setCanInvite(boolean canInvite) {
        this.canInvite = canInvite;
    }
}
