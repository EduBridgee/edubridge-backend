package com.upc.edubridge.auth.controller;

import com.upc.edubridge.auth.dto.LoginRequest;
import com.upc.edubridge.auth.dto.LoginResponse;
import com.upc.edubridge.auth.dto.ResetPasswordRequest;
import com.upc.edubridge.auth.service.AuthService;
import com.upc.edubridge.shared.config.JwtUtils;
import com.upc.edubridge.student.model.Student;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para el acceso y registro de usuarios en EduBridge")
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @Operation(
            summary = "Iniciar sesión",
            description = "Valida las credenciales del usuario (correo y contraseña). Si son correctas, genera y retorna un token JWT firmado junto con el rol del usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso. Devuelve token y perfil de usuario",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class)) }),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas o usuario no registrado",
                    content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Optional<Map<String, Object>> userMapOptional = authService.login(request);

            if (userMapOptional.isPresent()) {
                Map<String, Object> userMap = userMapOptional.get();

                if (Boolean.TRUE.equals(userMap.get("requires2fa"))) {
                    LoginResponse response = LoginResponse.builder()
                            .id(((Number) userMap.get("id")).longValue())
                            .name((String) userMap.get("name"))
                            .email((String) userMap.get("email"))
                            .role((String) userMap.get("role"))
                            .requires2fa(true)
                            .build();
                    return ResponseEntity.ok(response);
                }

                String email = (String) userMap.get("email");
                String role = (String) userMap.get("role");
                String name = (String) userMap.get("name");
                Long id = ((Number) userMap.get("id")).longValue();

                String token = jwtUtils.generateToken(email, role);

                LoginResponse response = LoginResponse.builder()
                        .token(token)
                        .id(id)
                        .name(name)
                        .email(email)
                        .role(role)
                        .requires2fa(false)
                        .build();

                return ResponseEntity.ok(response);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Correo o contraseña incorrectos"));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
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
    @Operation(summary = "Solicitar recuperación de contraseña")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        boolean result = authService.initiatePasswordRecovery(email);

        if (result) {
            return ResponseEntity.ok(Map.of("message", "Instrucciones enviadas al correo."));
        }
        
        return ResponseEntity.ok(Map.of("message", "Si el correo está registrado, recibirás instrucciones."));
    }
    @Operation(
            summary = "Restablecer contraseña",
            description = "Valida el token recibido por correo y actualiza la contraseña del usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Token inválido o expirado")
    })

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        boolean success = authService.resetPassword(request.getToken(), request.getNewPassword());
        return success
                ? ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."))
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "El token es inválido o ha expirado."));
    }

    @Operation(summary = "Consultar estado de 2FA", description = "Verifica si la autenticación de dos factores está activa para el email indicado.")
    @GetMapping("/2fa/status")
    public ResponseEntity<?> get2faStatus(@RequestParam String email) {
        boolean enabled = authService.is2faEnabled(email);
        return ResponseEntity.ok(Map.of("enabled", enabled));
    }

    @Operation(summary = "Activar 2FA", description = "Verifica el código OTP con la clave secreta y activa la autenticación de dos factores en la base de datos.")
    @PostMapping("/2fa/enable")
    public ResponseEntity<?> enable2fa(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String secret = request.get("secret");
        String code = request.get("code");

        if (email == null || secret == null || code == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Faltan parámetros requeridos"));
        }

        boolean verified = com.upc.edubridge.auth.utils.TotpUtils.verify(code, secret);
        if (!verified) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Código 2FA incorrecto o expirado"));
        }

        authService.enable2fa(email, secret);
        return ResponseEntity.ok(Map.of("success", true, "message", "Autenticación de dos factores activada correctamente"));
    }

    @Operation(summary = "Desactivar 2FA", description = "Desactiva la autenticación de dos factores en la base de datos.")
    @PostMapping("/2fa/disable")
    public ResponseEntity<?> disable2fa(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Falta el email"));
        }

        authService.disable2fa(email);
        return ResponseEntity.ok(Map.of("success", true, "message", "Autenticación de dos factores desactivada correctamente"));
    }
}