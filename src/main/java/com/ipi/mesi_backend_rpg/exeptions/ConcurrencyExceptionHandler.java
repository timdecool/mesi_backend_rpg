package com.ipi.mesi_backend_rpg.exeptions;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@ResponseBody
public class ConcurrencyExceptionHandler {
    
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLockingFailure(
            OptimisticLockingFailureException ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "CONCURRENT_MODIFICATION");
        response.put("message", "Les données ont été modifiées par un autre utilisateur pendant votre édition.");
        response.put("suggestion", "Rechargez la page et réappliquez vos modifications.");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
