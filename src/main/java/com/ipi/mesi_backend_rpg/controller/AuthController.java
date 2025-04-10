package com.ipi.mesi_backend_rpg.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    @GetMapping("/public/test")
    public ResponseEntity<Map<String, String>> publicEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Ceci est un endpoint public accessible sans authentification");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/secured/user-info")
    public ResponseEntity<Map<String, Object>> securedEndpoint() {
       
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("uid", authentication.getName());
        response.put("roles", authentication.getAuthorities());
        response.put("isAuthenticated", authentication.isAuthenticated());
        response.put("message", "Vous êtes correctement authentifié via Firebase!");

        return ResponseEntity.ok(response);
    }

}