package com.upc.edubridge.auth.service;

import com.upc.edubridge.auth.dto.LoginRequest;
import com.upc.edubridge.auth.model.PasswordResetToken;
import com.upc.edubridge.auth.model.User2faConfig;
import com.upc.edubridge.auth.repository.PasswordResetTokenRepository;
import com.upc.edubridge.auth.repository.User2faConfigRepository;
import com.upc.edubridge.auth.utils.TotpUtils;
import com.upc.edubridge.student.model.Student;
import com.upc.edubridge.student.repository.StudentRepository;
import com.upc.edubridge.teacher.model.Teacher;
import com.upc.edubridge.teacher.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final User2faConfigRepository user2faConfigRepository;

    public Optional<Map<String, Object>> login(LoginRequest request) {
        boolean credentialsValid = false;
        Map<String, Object> userMap = null;

        if ("admin@edubridge.com".equalsIgnoreCase(request.getEmail())) {
            if ("admin123".equals(request.getPassword())) {
                credentialsValid = true;
                userMap = Map.of("id", 999L, "name", "Admin", "email", "admin@edubridge.com", "role", "admin");
            }
        } else {
            Optional<Teacher> teacher = teacherRepository.findByEmail(request.getEmail());
            if (teacher.isPresent() && checkPassword(request.getPassword(), teacher.get().getPassword())) {
                credentialsValid = true;
                userMap = Map.of("id", teacher.get().getId(), "name", teacher.get().getName(), "email",
                        teacher.get().getEmail(), "role", "docente");
            } else {
                Optional<Student> student = studentRepository.findByEmail(request.getEmail());
                if (student.isPresent() && checkPassword(request.getPassword(), student.get().getPassword())) {
                    credentialsValid = true;
                    userMap = Map.of("id", student.get().getId(), "name", student.get().getName(), "email",
                            student.get().getEmail(), "role", "estudiante");
                }
            }
        }

        if (!credentialsValid) {
            return Optional.empty();
        }

        boolean is2faEnabled = is2faEnabled(request.getEmail());
        if (is2faEnabled) {
            if (request.getTwoFactorCode() != null && !request.getTwoFactorCode().trim().isEmpty()) {
                String secret = get2faSecret(request.getEmail());
                if (TotpUtils.verify(request.getTwoFactorCode().trim(), secret)) {
                    return Optional.of(userMap);
                } else {
                    throw new org.springframework.security.authentication.BadCredentialsException("Código 2FA incorrecto");
                }
            } else {
                return Optional.of(Map.of(
                    "requires2fa", true,
                    "email", userMap.get("email"),
                    "role", userMap.get("role"),
                    "name", userMap.get("name"),
                    "id", userMap.get("id")
                ));
            }
        }

        return Optional.of(userMap);
    }

    private boolean checkPassword(String raw, String encoded) {
        return (encoded != null && (encoded.startsWith("$2a$") || encoded.startsWith("$2b$")))
                ? passwordEncoder.matches(raw, encoded)
                : raw.equals(encoded);
    }

    public Student register(Student s) {
        s.setPassword(passwordEncoder.encode(s.getPassword()));
        s.setRole(s.getRole() == null || s.getRole().isEmpty() ? "estudiante" : s.getRole());
        return studentRepository.save(s);
    }

    public boolean initiatePasswordRecovery(String email) {
        if (studentRepository.findByEmail(email).isEmpty() && teacherRepository.findByEmail(email).isEmpty()) {
            return false;
        }
        String token = String.format("%06d", (int) (Math.random() * 1000000));
        tokenRepository.save(PasswordResetToken.builder()
                .token(token)
                .email(email)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build());
        emailService.sendRecoveryEmail(email, token);
        return true;
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);

        if (resetToken.isEmpty() || resetToken.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        String email = resetToken.get().getEmail();
        String encodedPassword = passwordEncoder.encode(newPassword);

        studentRepository.findByEmail(email).ifPresent(s -> {
            s.setPassword(encodedPassword);
            studentRepository.save(s);
        });
        teacherRepository.findByEmail(email).ifPresent(t -> {
            t.setPassword(encodedPassword);
            teacherRepository.save(t);
        });

        tokenRepository.delete(resetToken.get());
        return true;
    }

    public boolean is2faEnabled(String email) {
        if (email == null) return false;
        return user2faConfigRepository.findByEmail(email.toLowerCase().trim())
                .map(User2faConfig::isEnabled)
                .orElse(false);
    }

    public String get2faSecret(String email) {
        if (email == null) return null;
        return user2faConfigRepository.findByEmail(email.toLowerCase().trim())
                .map(User2faConfig::getSecret)
                .orElse(null);
    }

    public void enable2fa(String email, String secret) {
        if (email == null) return;
        String cleanEmail = email.toLowerCase().trim();
        User2faConfig config = user2faConfigRepository.findByEmail(cleanEmail)
                .orElse(new User2faConfig());
        config.setEmail(cleanEmail);
        config.setEnabled(true);
        config.setSecret(secret);
        user2faConfigRepository.save(config);
    }

    public void disable2fa(String email) {
        if (email == null) return;
        String cleanEmail = email.toLowerCase().trim();
        user2faConfigRepository.findByEmail(cleanEmail).ifPresent(config -> {
            config.setEnabled(false);
            user2faConfigRepository.save(config);
        });
    }
}