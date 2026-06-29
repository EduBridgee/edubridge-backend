package com.upc.edubridge.student.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_tasks")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String courseName;
    private String dueDate;
    private String status;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    private String teacherName;
}
