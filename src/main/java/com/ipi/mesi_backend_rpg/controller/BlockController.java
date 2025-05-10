package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
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

    @GetMapping("/module-version/{moduleVersionId}")
    public ResponseEntity<List<BlockDTO>> getBlocksByModuleVersionId(@PathVariable Long moduleVersionId) {
        List<BlockDTO> blocks = blockService.getAllBlocksByModuleVersionId(moduleVersionId);
        return ResponseEntity.ok(blocks);
    }
    
    @PostMapping
    public ResponseEntity<BlockDTO> createBlock(@Valid @RequestBody BlockDTO blockDTO) {
        BlockDTO createdBlock = blockService.createBlock(blockDTO);
        return new ResponseEntity<>(createdBlock, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BlockDTO> updateBlock(@PathVariable Long id, @Valid @RequestBody BlockDTO blockDTO) {
        BlockDTO updatedBlock = blockService.updateBlock(id, blockDTO);
        return ResponseEntity.ok(updatedBlock);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlock(@PathVariable Long id) {
        blockService.deleteBlock(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/batch")
    public ResponseEntity<Void> createOrUpdateBlocks(@Valid @RequestBody List<BlockDTO> blockDTOs) {
        blockService.createOrUpdateBlocks(blockDTOs);
        return ResponseEntity.ok().build();
    }
}
