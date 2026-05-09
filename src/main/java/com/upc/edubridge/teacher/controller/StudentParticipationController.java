package com.upc.edubridge.teacher.controller;

import com.upc.edubridge.teacher.model.StudentParticipation;
import com.upc.edubridge.teacher.repository.StudentParticipationRepository;
import com.upc.edubridge.student.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/participations")
public class StudentParticipationController {

    @Autowired
    private StudentParticipationRepository participationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @PostMapping
    public StudentParticipation save(@RequestBody Map<String, Object> payload) {
        Long studentId = Long.valueOf(payload.get("studentId").toString());

        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        StudentParticipation p = StudentParticipation.builder()
                .student(student)
                .points(Integer.parseInt(payload.get("points").toString()))
                .observation(payload.get("observation").toString())
                .registrationDate(LocalDateTime.parse(payload.get("registrationDate").toString() + "T00:00:00"))
                .build();

        return participationRepository.save(p);
    }
}