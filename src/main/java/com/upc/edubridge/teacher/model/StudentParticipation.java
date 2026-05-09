package com.upc.edubridge.teacher.model;

import com.upc.edubridge.student.model.Student;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_participations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    private Integer points;
    private String observation;
    private LocalDateTime registrationDate;
}