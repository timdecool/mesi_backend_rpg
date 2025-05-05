package com.ipi.mesi_backend_rpg.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ErrorValidation {

    private String input;
    private String error;

    public ErrorValidation(String input, String error) {
        this.input = input;
        this.error = error;
    }
}
