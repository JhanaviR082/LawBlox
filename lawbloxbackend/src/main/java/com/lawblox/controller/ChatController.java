package com.lawblox.controller;

import com.lawblox.dto.ChatRequest;
import com.lawblox.service.LegalChatService;
import com.lawblox.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {
    
    private final LegalChatService legalChatService;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7); // Remove "Bearer "
        String email = jwtUtil.extractEmail(token);
        
        Map<String, Object> response = legalChatService.processMessage(
            request.getMessage(), 
            email
        );
        
        return ResponseEntity.ok(response);
    }
}