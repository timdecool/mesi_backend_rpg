package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.GameSystemDTO;
import com.ipi.mesi_backend_rpg.mapper.GameSystemMapper;
import com.ipi.mesi_backend_rpg.model.GameSystem;
import com.ipi.mesi_backend_rpg.repository.GameSystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameSystemService {

    private final GameSystemRepository gameSystemRepository;
    private final GameSystemMapper gameSystemMapper;

    public GameSystemDTO createGameSystem(GameSystemDTO gameSystemDTO) {
        GameSystem gameSystem = new GameSystem();
        gameSystem.setName(gameSystemDTO.name());
        gameSystem.setCreatedAt(LocalDate.now());
        gameSystem.setUpdatedAt(LocalDate.now());
        gameSystemRepository.save(gameSystem);
        return gameSystemMapper.toDTO(gameSystem);
    }

    public List<GameSystemDTO> getAllGameSystems() {
        List<GameSystem> gameSystems = gameSystemRepository.findAll();
        return gameSystems.stream().map(gameSystemMapper::toDTO).toList();
    }

    public GameSystemDTO getGame(Long id) {
        GameSystem gameSystem = gameSystemRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return gameSystemMapper.toDTO(gameSystem);
    }

    public GameSystemDTO updateGameSystem(GameSystemDTO gameSystemDTO, Long id) {

        GameSystem gameSystem = gameSystemRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        gameSystem.setName(gameSystemDTO.name());
        gameSystem.setUpdatedAt(LocalDate.now());

        GameSystem updatedGameSystem = gameSystemRepository.save(gameSystem);
        
        return gameSystemMapper.toDTO(updatedGameSystem);
    }

    public GameSystemDTO deleteGameSystem(Long id) {
        GameSystem gameSystem = gameSystemRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        gameSystemRepository.delete(gameSystem);
        return gameSystemMapper.toDTO(gameSystem);
    }
}
