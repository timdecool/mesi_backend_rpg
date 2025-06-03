package com.ipi.mesi_backend_rpg.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ModuleUpdateDTO {
    private Long userId;
    private String username;
    private String blockId;
    private String operation; // "insert", "delete", "update"
    private String content;
    private Integer startPosition;
    private Integer endPosition;
    private Long timestamp;
}