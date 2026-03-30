package com.chamikara.spring_backend.service;

import com.chamikara.spring_backend.dto.request.TransformerRequest;
import com.chamikara.spring_backend.dto.response.TransformerResponse;
import com.chamikara.spring_backend.entity.Transformer;
import com.chamikara.spring_backend.exception.ResourceNotFoundException;
import com.chamikara.spring_backend.exception.DuplicateResourceException;
import com.chamikara.spring_backend.repository.TransformerRepository;
import com.chamikara.spring_backend.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransformerService {
    
    private final TransformerRepository transformerRepository;
    private final CurrentUserService currentUserService;
    
    public List<TransformerResponse> getAllTransformers() {
        log.debug("Fetching all transformers");
        return transformerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public TransformerResponse getTransformerById(Long id) {
        log.debug("Fetching transformer with id: {}", id);
        Transformer transformer = transformerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transformer", "id", id));
        return mapToResponse(transformer);
    }
    
    public TransformerResponse createTransformer(TransformerRequest request) {
        log.debug("Creating new transformer with number: {}", request.getNumber());
        Long currentUserId = currentUserService.getCurrentUserId();
        
        if (transformerRepository.existsByNumber(request.getNumber())) {
            throw new DuplicateResourceException("Transformer", "number", request.getNumber());
        }
        
        Transformer transformer = Transformer.builder()
                .userId(currentUserId)
                .number(request.getNumber())
                .pole(request.getPole())
                .region(request.getRegion())
                .type(request.getType())
                .baselineImage(request.getBaselineImage())
                .weather(request.getWeather())
                .location(request.getLocation())
                .build();
        
        Transformer saved = transformerRepository.save(transformer);
        log.info("Created transformer with id: {} for local user: {}", saved.getId(), currentUserId);
        return mapToResponse(saved);
    }
    
    public TransformerResponse updateTransformer(Long id, TransformerRequest request) {
        log.debug("Updating transformer with id: {}", id);
        
        Transformer transformer = transformerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transformer", "id", id));
        
        // Check for duplicate number if changed
        if (!transformer.getNumber().equals(request.getNumber()) 
                && transformerRepository.existsByNumber(request.getNumber())) {
            throw new DuplicateResourceException("Transformer", "number", request.getNumber());
        }
        
        transformer.setNumber(request.getNumber());
        transformer.setPole(request.getPole());
        transformer.setRegion(request.getRegion());
        transformer.setType(request.getType());
        transformer.setBaselineImage(request.getBaselineImage());
        transformer.setWeather(request.getWeather());
        transformer.setLocation(request.getLocation());
        
        Transformer updated = transformerRepository.save(transformer);
        log.info("Updated transformer with id: {}", id);
        return mapToResponse(updated);
    }
    
    public void deleteTransformer(Long id) {
        log.debug("Deleting transformer with id: {}", id);
        
        if (!transformerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transformer", "id", id);
        }
        
        transformerRepository.deleteById(id);
        log.info("Deleted transformer with id: {}", id);
    }
    
    public Transformer getTransformerEntity(Long id) {
        return transformerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transformer", "id", id));
    }
    
    private TransformerResponse mapToResponse(Transformer transformer) {
        return TransformerResponse.builder()
                .id(transformer.getId())
                .number(transformer.getNumber())
                .pole(transformer.getPole())
                .region(transformer.getRegion())
                .type(transformer.getType())
                .baselineImage(transformer.getBaselineImage())
                .baselineUploadDate(transformer.getCreatedAt() != null ? transformer.getCreatedAt().toString() : null)
                .weather(transformer.getWeather())
                .location(transformer.getLocation())
                .inspectionCount(transformer.getInspections() != null ? transformer.getInspections().size() : 0)
                .build();
    }
}
