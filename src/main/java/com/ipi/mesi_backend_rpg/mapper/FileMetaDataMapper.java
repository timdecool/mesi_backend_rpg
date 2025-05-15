package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.FileMetaDataDTO;
import com.ipi.mesi_backend_rpg.model.FileMetaData;
import org.springframework.stereotype.Service;

@Service
public class FileMetaDataMapper {

    public FileMetaDataDTO toDTO(FileMetaData fileMetaData) {
        return new FileMetaDataDTO(
                fileMetaData.getUniqueId(),
                fileMetaData.getObjectName(),
                fileMetaData.getUploadDate(),
                fileMetaData.getContentType(),
                fileMetaData.getPublicUrl()
        );
    }

    public FileMetaData toEntity(FileMetaDataDTO fileMetaDataDTO) {

        return new FileMetaData(
                fileMetaDataDTO.uniqueId(),
                fileMetaDataDTO.objectName(),
                fileMetaDataDTO.uploadDate(),
                fileMetaDataDTO.contentType(),
                fileMetaDataDTO.publicUrl()
        );
    }


}
