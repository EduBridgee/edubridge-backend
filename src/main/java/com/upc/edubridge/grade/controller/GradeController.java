package com.upc.edubridge.grade.controller;

import com.upc.edubridge.course.model.Course;
import com.upc.edubridge.course.repository.CourseRepository;
import com.upc.edubridge.grade.model.Grade;
import com.upc.edubridge.grade.repository.GradeRepository;
import com.upc.edubridge.student.model.Student;
import com.upc.edubridge.student.repository.StudentRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor // Inyección automática por constructor
public class GradeController {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @GetMapping
    public ResponseEntity<List<Grade>> getAllGrades() {
        return ResponseEntity.ok(gradeRepository.findAll());
    }
    @PostMapping
    @Transactional
    public ResponseEntity<?> registrarNota(@RequestBody GradeRequest request) {
        if (request.getValue() < 0 || request.getValue() > 20) {
            return ResponseEntity.badRequest().body("La nota debe estar entre 0 y 20");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElse(null);
        if (student == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Estudiante no encontrado");

        Course course = courseRepository.findById(request.getCourseId())
                .orElse(null);
        if (course == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Curso no encontrado");

        Grade grade = Grade.builder()
                .value(request.getValue())
                .student(student)
                .course(course)
                .build();

        gradeRepository.save(grade);

        actualizarPromedioEstudiante(student);

        return ResponseEntity.status(HttpStatus.CREATED).body(grade);
    }


    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Grade>> getGradesByStudent(@PathVariable Long studentId) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        return ResponseEntity.ok(grades);
    }

    private void actualizarPromedioEstudiante(Student student) {
        List<Grade> notas = gradeRepository.findByStudentId(student.getId());

        if (!notas.isEmpty()) {
            double promedio = notas.stream()
                    .mapToDouble(Grade::getValue)
                    .average()
                    .orElse(0.0);

            double promedioRedondeado = Math.round(promedio * 10.0) / 10.0;

            student.setAverageGrade(promedioRedondeado);
            studentRepository.save(student);
        }
    }
}

@Data
class GradeRequest {
    private Long studentId;
    private Long courseId;
    private Double value;
}