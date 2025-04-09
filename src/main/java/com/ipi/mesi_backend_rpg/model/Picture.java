package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Picture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PictureUsage pictureUsage;

    private Long pictureUsageId;

    private String title;
    private String src;
    private LocalDate createdAt;
    private LocalDate updateAt;

    public Picture(PictureUsage pictureUsage, Long pictureUsageId, String title, String src, LocalDate createdAt, LocalDate updateAt) {
        this.pictureUsage = pictureUsage;
        this.pictureUsageId = pictureUsageId;
        this.title = title;
        this.src = src;
        this.createdAt = createdAt;
        this.updateAt = updateAt;
    }
}
