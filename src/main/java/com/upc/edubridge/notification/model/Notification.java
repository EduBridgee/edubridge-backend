package com.upc.edubridge.notification.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private String type;
    private String message;

    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean isRead = false;
}