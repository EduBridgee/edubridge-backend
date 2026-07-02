package com.upc.edubridge.enrollment.controller;

import com.upc.edubridge.enrollment.model.Enrollment;
import com.upc.edubridge.enrollment.repository.EnrollmentRepository;
import com.upc.edubridge.course.model.Course;
import com.upc.edubridge.course.repository.CourseRepository;
import com.upc.edubridge.student.model.Student;
import com.upc.edubridge.student.repository.StudentRepository;
import com.upc.edubridge.teacher.model.StudentParticipation;
import com.upc.edubridge.teacher.repository.StudentParticipationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Matrículas", description = "Endpoints para gestionar la inscripción de alumnos en cursos")
public class EnrollmentController {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final StudentParticipationRepository studentParticipationRepository;

    @Operation(summary = "Obtener cursos de un estudiante")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentRepository.findByStudentId(studentId));
    }

    @Operation(summary = "Obtener estudiantes matriculados en un curso")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentRepository.findByCourseId(courseId));
    }

    @Operation(summary = "Matricular estudiante", description = "Crea una nueva matrícula o reactiva una CANCELADA.")
    @PostMapping
    public ResponseEntity<?> enrollStudent(@RequestBody EnrollmentRequest request) {
        Optional<Enrollment> existingEnrollment = enrollmentRepository.findAll().stream()
                .filter(e -> e.getStudent().getId().equals(request.getStudentId()) &&
                        e.getCourse().getId().equals(request.getCourseId()))
                .findFirst();

        if (existingEnrollment.isPresent()) {
            Enrollment e = existingEnrollment.get();
            if ("CANCELADO".equalsIgnoreCase(e.getStatus())) {
                e.setStatus("PENDIENTE");
                enrollmentRepository.save(e);
                return ResponseEntity.ok(e);
            }
            return ResponseEntity.badRequest().body(Map.of("message", "El estudiante ya tiene una matrícula activa o pendiente."));
        }

        Student student = studentRepository.findById(request.getStudentId()).orElse(null);
        Course course = courseRepository.findById(request.getCourseId()).orElse(null);

        if (student == null || course == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Estudiante o Curso no encontrado."));
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .semester(request.getSemester())
                .status("PENDIENTE")
                .build();

        enrollmentRepository.save(enrollment);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    @Operation(summary = "Obtener todas las matrículas")
    @GetMapping
    public ResponseEntity<List<Enrollment>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentRepository.findAll());
    }

    @Operation(summary = "Actualizar estado de matrícula", description = "Cambia el estado (PENDIENTE -> APROBADO/CANCELADO).")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateEnrollmentStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return enrollmentRepository.findById(id).map(enrollment -> {
            String nuevoEstado = body.get("status");
            if (nuevoEstado != null) {
                enrollment.setStatus(nuevoEstado.toUpperCase());
                enrollmentRepository.save(enrollment);
                return ResponseEntity.ok(Map.of("message", "Estado actualizado a " + nuevoEstado));
            }
            return ResponseEntity.badRequest().body(Map.of("message", "Estado no proporcionado"));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Matrícula no encontrada")));
    }

    @Operation(summary = "Registrar asistencia masiva")
    @PostMapping("/attendance/bulk")
    public ResponseEntity<?> registrarAsistenciaCurso(@RequestBody List<AttendanceRecordRequest> records) {
        records.forEach(record -> {
            enrollmentRepository.findByStudentId(record.getStudentId()).stream()
                    .filter(e -> e.getCourse().getId().equals(record.getCourseId()))
                    .findFirst().ifPresent(enrollment -> {
                        enrollment.setTotalClasses(enrollment.getTotalClasses() + 1);
                        if (record.isPresent()) enrollment.setAttendedClasses(enrollment.getAttendedClasses() + 1);
                        else enrollment.setAbsences(enrollment.getAbsences() + 1);
                        enrollment.actualizarPorcentaje();
                        enrollmentRepository.save(enrollment);
                    });
        });
        return ResponseEntity.ok(Map.of("status", "success", "message", "Asistencia actualizada."));
    }

    @Operation(summary = "Registrar participaciones masivas")
    @PostMapping("/participations/bulk")
    public ResponseEntity<?> registrarParticipacionesCurso(@RequestBody List<ParticipationRecordRequest> records) {
        records.forEach(record -> {
            enrollmentRepository.incrementarPuntosParticipacion(record.getStudentId(), record.getCourseId(), record.getPoints());
            studentRepository.findById(record.getStudentId()).ifPresent(student -> {
                studentParticipationRepository.save(StudentParticipation.builder()
                        .student(student).points(record.getPoints()).observation("Participación registrada")
                        .registrationDate(LocalDateTime.now()).build());
            });
        });
        return ResponseEntity.ok(Map.of("status", "success", "message", "Puntos sincronizados."));
    }
}

@Data class EnrollmentRequest { private Long studentId; private Long courseId; private String semester; }
@Data class AttendanceRecordRequest { private Long studentId; private Long courseId; private boolean present; }
@Data class ParticipationRecordRequest { private Long studentId; private Long courseId; private int points; }