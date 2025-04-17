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
public class UserFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long folder_id;
    
    private Long user_id;

    private String name;

    private Long parent_folder;

    public UserFolder(Long folder_id, Long user_id, String name, Long parent_folder){
        this.folder_id = folder_id;
        this.user_id = user_id;
        this.name = name;
        this.parent_folder = parent_folder;
    }
}
