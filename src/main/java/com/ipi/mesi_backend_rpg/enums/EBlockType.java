package com.ipi.mesi_backend_rpg.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EBlockType {
    PARAGRAPH("paragraph"),
    MODULE("module"),
    STAT("stat"),
    MUSIC("music");
    
    private final String value;
    
    EBlockType(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
}