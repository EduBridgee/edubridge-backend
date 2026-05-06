package com.upc.edubridge.auth.service;

import com.upc.edubridge.auth.dto.LoginRequest;
import com.upc.edubridge.student.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private StudentRepository studentRepository;

    public Optional<Map<String, Object>> login(LoginRequest request) {
        if ("petermontalvo@upc.edu.pe".equals(request.getEmail()) &&
                "PeterMontalvo".equals(request.getPassword())) {
            return Optional.of(Map.of(
                    "id", 0L,
                    "name", "Peter Montalvo",
                    "role", "docente"
            ));
        }

        return studentRepository.findByEmail(request.getEmail())
                .filter(student -> student.getPassword().equals(request.getPassword()))
                .map(student -> Map.of(
                        "id", student.getId(),
                        "name", student.getName(),
                        "role", "estudiante"
                ));
    }
}