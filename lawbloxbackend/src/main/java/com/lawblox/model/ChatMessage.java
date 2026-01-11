package com.lawblox.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")  // matches SQL
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")  // matches SQL foreign key
    private User user;

    @Column(name = "message_text", columnDefinition = "TEXT", nullable = false)
    private String userMessage;

    @Column(name = "bot_response", columnDefinition = "TEXT")
    private String botResponse;

    @Column(name = "detected_keywords", columnDefinition = "TEXT")
    private String detectedKeywords;

    @Column(name = "created_at")
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
