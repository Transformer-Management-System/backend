package com.chamikara.spring_backend.controller;

import com.chamikara.spring_backend.dto.request.TransformerRequest;
import com.chamikara.spring_backend.dto.response.ApiResponse;
import com.chamikara.spring_backend.dto.response.TransformerResponse;
import com.chamikara.spring_backend.service.TransformerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transformers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TransformerController {
    
    private final TransformerService transformerService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<TransformerResponse>>> getAllTransformers() {
        log.info("GET /transformers - Fetching all transformers");
        List<TransformerResponse> transformers = transformerService.getAllTransformers();
        return ResponseEntity.ok(ApiResponse.success("Transformers retrieved successfully", transformers));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransformerResponse>> getTransformerById(@PathVariable Long id) {
        log.info("GET /transformers/{} - Fetching transformer", id);
        TransformerResponse transformer = transformerService.getTransformerById(id);
        return ResponseEntity.ok(ApiResponse.success("Transformer retrieved successfully", transformer));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<TransformerResponse>> createTransformer(
            @Valid @RequestBody TransformerRequest request) {
        log.info("POST /transformers - Creating new transformer: {}", request.getNumber());
        TransformerResponse created = transformerService.createTransformer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transformer created successfully", created));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransformerResponse>> updateTransformer(
            @PathVariable Long id,
            @Valid @RequestBody TransformerRequest request) {
        log.info("PUT /transformers/{} - Updating transformer", id);
        TransformerResponse updated = transformerService.updateTransformer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Transformer updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransformer(@PathVariable Long id) {
        log.info("DELETE /transformers/{} - Deleting transformer", id);
        transformerService.deleteTransformer(id);
        return ResponseEntity.ok(ApiResponse.success("Transformer deleted successfully", null));
    }
}
