package com.ipi.mesi_backend_rpg.dto;

import com.ipi.mesi_backend_rpg.model.Module;

import java.util.List;

public record TagDTO (
        Integer id,
        String name,
        List<Module> modules
){

}
