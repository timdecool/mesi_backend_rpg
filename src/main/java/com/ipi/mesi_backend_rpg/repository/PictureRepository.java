package com.ipi.mesi_backend_rpg.repository;

import com.ipi.mesi_backend_rpg.model.Picture;
import com.ipi.mesi_backend_rpg.model.PictureUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PictureRepository extends JpaRepository<Picture, Long> {
    List<Picture> findByPictureUsageAndPictureUsageId(PictureUsage pictureUsage, Long pictureUsageId);
}
