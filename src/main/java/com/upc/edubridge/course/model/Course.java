package com.upc.edubridge.course.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "course")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String code;
    private Integer credits;
    private String category;
    private String icon;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    @JsonIgnoreProperties("course")
    private com.upc.edubridge.teacher.model.Teacher teacher;
}