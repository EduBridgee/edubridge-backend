package com.upc.edubridge.student.dto;

import lombok.Data;

@Data
public class StudentTaskRequest {
    private Long studentId;
    private String title;
    private String courseName;
    private String dueDate;
    private String teacherName;
}
