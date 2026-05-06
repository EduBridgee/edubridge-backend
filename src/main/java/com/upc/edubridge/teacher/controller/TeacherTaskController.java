package com.upc.edubridge.teacher.controller;

import com.upc.edubridge.teacher.model.*;
import com.upc.edubridge.teacher.repository.TeacherTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teacher-tasks")
public class TeacherTaskController {

    @Autowired
    private TeacherTaskRepository teacherTaskRepository;

    @GetMapping
    public List<TeacherTask> getAll() {
        return teacherTaskRepository.findAll();
    }
}