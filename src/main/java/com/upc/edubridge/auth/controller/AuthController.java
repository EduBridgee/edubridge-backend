package com.upc.edubridge.auth.controller;

import com.upc.edubridge.auth.dto.LoginRequest;
import com.upc.edubridge.auth.service.AuthService;
import com.upc.edubridge.student.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return authService.login(request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Correo o contraseña incorrectos")));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Student student) {
        try {
            Student registeredStudent = authService.register(student);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Estudiante registrado exitosamente",
                    "studentId", registeredStudent.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Error al registrar el estudiante: " + e.getMessage()));
        }
    }
}