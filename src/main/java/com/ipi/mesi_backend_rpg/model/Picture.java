package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Picture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PictureUsage pictureUsage;

    private Long pictureUsageId;

    private String title;
    private String src;
    private LocalDate createAt;
    private LocalDate updateAt;

    public Picture() {
    }

    public Picture(PictureUsage pictureUsage, Long pictureUsageId, String title, String src, LocalDate createAt, LocalDate updateAt) {
        this.pictureUsage = pictureUsage;
        this.pictureUsageId = pictureUsageId;
        this.title = title;
        this.src = src;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PictureUsage getPictureUsage() {
        return pictureUsage;
    }

    public void setPictureUsage(PictureUsage pictureUsage) {
        this.pictureUsage = pictureUsage;
    }

    public Long getPictureUsageId() {
        return pictureUsageId;
    }

    public void setPictureUsageId(Long pictureUsageId) {
        this.pictureUsageId = pictureUsageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public LocalDate getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDate createAt) {
        this.createAt = createAt;
    }

    public LocalDate getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDate updateAt) {
        this.updateAt = updateAt;
    }
}
