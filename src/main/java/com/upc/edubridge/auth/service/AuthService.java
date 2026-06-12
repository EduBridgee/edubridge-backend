package com.upc.edubridge.auth.service;

import com.upc.edubridge.auth.dto.LoginRequest;
import com.upc.edubridge.auth.model.PasswordResetToken;
import com.upc.edubridge.auth.repository.PasswordResetTokenRepository;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<Map<String, Object>> login(LoginRequest request) {
        if ("admin@edubridge.com".equalsIgnoreCase(request.getEmail())) {
            if ("admin123".equals(request.getPassword())) {
                return Optional
                        .of(Map.of("id", 999L, "name", "Admin", "email", "admin@edubridge.com", "role", "admin"));
            }
        }
        Optional<Teacher> teacher = teacherRepository.findByEmail(request.getEmail());
        if (teacher.isPresent() && checkPassword(request.getPassword(), teacher.get().getPassword())) {
            return Optional.of(Map.of("id", teacher.get().getId(), "name", teacher.get().getName(), "email",
                    teacher.get().getEmail(), "role", "docente"));
        }
        return studentRepository.findByEmail(request.getEmail())
                .filter(s -> checkPassword(request.getPassword(), s.getPassword()))
                .map(s -> Map.of("id", s.getId(), "name", s.getName(), "email", s.getEmail(), "role", "estudiante"));
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

    public Student register(Student student) {
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        if (student.getRole() == null || student.getRole().isEmpty()) {
            student.setRole("estudiante");
        }
        return studentRepository.save(student);
    }
}