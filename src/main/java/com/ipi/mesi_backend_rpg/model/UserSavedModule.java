package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserSavedModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long saved_module_id;

    private Long user_id;

    private Long module_id;

    private Long module_version_id;

    private Long folder_id;

    private String alias;

    public UserSavedModule(Long saved_module_id, Long user_id, Long module_id, Long module_version_id, Long folder_id,
            String alias) {
        this.saved_module_id = saved_module_id;
        this.user_id = user_id;
        this.module_id = module_id;
        this.module_version_id = module_version_id;
        this.folder_id = folder_id;
        this.alias = alias;
    }
}
