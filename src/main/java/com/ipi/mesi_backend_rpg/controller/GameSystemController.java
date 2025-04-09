package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.GameSystemDTO;
import com.ipi.mesi_backend_rpg.service.GameSystemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/game-system")
@RequiredArgsConstructor
public class GameSystemController {

    private final GameSystemService gameSystemService;

    @PostMapping
    public ResponseEntity<GameSystemDTO> createGameSystem(@Valid @RequestBody GameSystemDTO gameSystemDTO) {
        GameSystemDTO gameSystem = gameSystemService.createGameSystem(gameSystemDTO);
        return new ResponseEntity<>(gameSystem, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameSystemDTO> getGameSystem(@PathVariable Long id) {
        GameSystemDTO gameSystem = gameSystemService.getGame(id);
        return new ResponseEntity<>(gameSystem, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<GameSystemDTO>> getGameSystems() {
        List<GameSystemDTO> gameSystems = gameSystemService.getAllGameSystems();
        return new ResponseEntity<>(gameSystems, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GameSystemDTO> updateGameSystem(@PathVariable Long id, @Valid @RequestBody GameSystemDTO gameSystemDTO) {
        GameSystemDTO gameSystem = gameSystemService.updateGameSystem(gameSystemDTO, id);
        return new ResponseEntity<>(gameSystem, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GameSystemDTO> deleteGameSystem(@PathVariable Long id) {
        GameSystemDTO gameSystemDTO = gameSystemService.deleteGameSystem(id);
        return new ResponseEntity<>(gameSystemDTO, HttpStatus.NO_CONTENT);
    }

}
