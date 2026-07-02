package com.upc.edubridge.tutoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tutoring_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutoringSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseName;
    private String teacherName;
    private String topic;
    private LocalDateTime startTime;
    private Integer durationMinutes;
    private Integer studentCount;
    private String status;
    private String type;
    private Integer rating;
}