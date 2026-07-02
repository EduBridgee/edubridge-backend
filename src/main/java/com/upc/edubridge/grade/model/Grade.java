package com.upc.edubridge.grade.model;

import com.upc.edubridge.course.model.Course;
import com.upc.edubridge.student.model.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grade")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double value;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_type")
    private EvaluationType type;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}