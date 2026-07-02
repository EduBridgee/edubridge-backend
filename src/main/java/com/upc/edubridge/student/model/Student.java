package com.upc.edubridge.student.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;
    private String role;

    @Column(name = "risk_level")
    private String riskLevel;

    private String grade;

    @Column(name = "average_grade")
    private Double averageGrade;

    private Integer absences;

    @Column(name = "total_classes")
    private Integer totalClasses;

    @Column(name = "attended_classes")
    private Integer attendedClasses;

    @Column(name = "attendance_percentage")
    private Integer attendancePercentage;
    private String code;
    private String program;
    private String semester;
    private String status;

    private String phone;
    private String address;
}