package com.ipi.mesi_backend_rpg.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CursorPositionDTO {
    private Long userId;
    private String username;
    private String blockId;
    private Position position;
    private String userColor; // Pour distinguer les curseurs des différents utilisateurs
    private String elementId;
}