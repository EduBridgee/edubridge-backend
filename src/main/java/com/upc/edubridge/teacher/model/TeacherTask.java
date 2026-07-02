package com.upc.edubridge.teacher.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "teacher_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String tag;
    private String status;
    private Integer progress;
    private String dueDate;
}