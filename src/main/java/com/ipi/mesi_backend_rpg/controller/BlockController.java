package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.service.BlockService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/block")
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @GetMapping("/module-version/{module-version}")
    public ResponseEntity<List<BlockDTO>> getAllBlocks(@PathVariable(name = "module-version", required = true) ModuleVersion moduleVersion) {
        List<BlockDTO> blocks = blockService.getAllBlocks(moduleVersion);
        return new ResponseEntity<>(blocks, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<BlockDTO> createBlock(@Valid @RequestBody BlockDTO blockDTO) {
        BlockDTO block = blockService.createBlock(blockDTO);
        return new ResponseEntity<>(block, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlockDTO> updateBlock(@PathVariable Long id, @Valid @RequestBody BlockDTO blockDTO) {
        BlockDTO block = blockService.updateBlock(id, blockDTO);
        return new ResponseEntity<>(block, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BlockDTO> deleteBlock(@PathVariable Long id) {
        BlockDTO block = blockService.deleteBlock(id);
        return new ResponseEntity<>(block, HttpStatus.NO_CONTENT);
    }
}
