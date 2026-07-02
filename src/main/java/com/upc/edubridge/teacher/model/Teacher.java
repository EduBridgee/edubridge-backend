package com.upc.edubridge.teacher.model;

import com.upc.edubridge.course.model.Course;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "teachers")
@Data
@NoArgsConstructor
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String specialization;

    private String role = "DOCENTE";

    @OneToMany(mappedBy = "teacher", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("teacher")
    private java.util.List<Course> courses = new java.util.ArrayList<>();

    public Course getCourse() {
        return (courses != null && !courses.isEmpty()) ? courses.get(0) : null;
    }

    public void setCourse(Course course) {
        if (this.courses == null) {
            this.courses = new java.util.ArrayList<>();
        }
        this.courses.clear();
        if (course != null) {
            this.courses.add(course);
        }
    }
}