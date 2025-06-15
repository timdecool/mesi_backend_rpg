package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate updatedAt;
    
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;
    
    @Column(name = "profile_views", nullable = false)
    private Long profileViews = 0L;

    @OneToOne
    private User user;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Picture picture;

    public UserProfile(String description, LocalDate createdAt, LocalDate updatedAt) {
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isPublic = true;
        this.profileViews = 0L;
    }

}
