package com.upc.edubridge.course.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String code;
    private int credits;
    private String category;
    private String icon;
}