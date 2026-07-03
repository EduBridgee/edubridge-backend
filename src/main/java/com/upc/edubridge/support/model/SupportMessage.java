package com.upc.edubridge.support.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_messages")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    @JsonIgnore
    private SupportTicket ticket;

    private String senderRole;
    private String senderName;
    @Column(length = 2000)
    private String message;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
