package com.upc.edubridge.auth.controller;

import com.upc.edubridge.auth.dto.LoginRequest;
import com.upc.edubridge.auth.service.AuthService;
import com.upc.edubridge.student.model.Student;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para el acceso y registro de usuarios en EduBridge")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(
            summary = "Iniciar sesión",
            description = "Valida las credenciales del usuario (correo y contraseña) para permitir el acceso al sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso",
                    content = { @Content(mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
                    content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return authService.login(request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Correo o contraseña incorrectos")));
    }

    @Operation(
            summary = "Registrar nuevo estudiante",
            description = "Crea una nueva cuenta de estudiante en la plataforma EduBridge."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Estudiante registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Error en los datos de registro o correo ya existente")
    })
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