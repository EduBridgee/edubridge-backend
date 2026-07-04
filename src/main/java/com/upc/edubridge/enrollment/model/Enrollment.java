package com.upc.edubridge.enrollment.model;

import com.upc.edubridge.course.model.Course;
import com.upc.edubridge.student.model.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private String semester;
    private String status;

    @Builder.Default
    private LocalDateTime enrollmentDate = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false, columnDefinition = "int default 0")
    private int totalClasses = 0;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "int default 0")
    private int attendedClasses = 0;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "int default 0")
    private int absences = 0;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "int default 0")
    private int attendancePercentage = 0;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "int default 0")
    private int participations = 0;

    public void actualizarPorcentaje() {
        if (this.totalClasses > 0) {
            this.attendancePercentage = (int) Math.round(((double) this.attendedClasses / this.totalClasses) * 100);
        } else {
            this.attendancePercentage = 0;
        }
    }
}