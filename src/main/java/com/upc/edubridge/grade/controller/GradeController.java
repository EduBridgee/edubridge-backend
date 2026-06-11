package com.upc.edubridge.grade.controller;

import com.upc.edubridge.course.model.Course;
import com.upc.edubridge.course.repository.CourseRepository;
import com.upc.edubridge.grade.model.Grade;
import com.upc.edubridge.grade.repository.GradeRepository;
import com.upc.edubridge.student.model.Student;
import com.upc.edubridge.student.repository.StudentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@Tag(name = "Calificaciones", description = "Endpoints para la gestión de notas y cálculo de promedios académicos")
public class GradeController {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Operation(summary = "Listar todas las notas", description = "Retorna un historial completo de todas las calificaciones registradas en el sistema.")
    @GetMapping
    public ResponseEntity<List<Grade>> getAllGrades() {
        return ResponseEntity.ok(gradeRepository.findAll());
    }

    @Operation(
            summary = "Registrar nueva calificación",
            description = "Registra una nota para un estudiante en un curso específico. Automáticamente dispara el recalculo del promedio general del estudiante."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Nota registrada y promedio actualizado",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Grade.class)) }),
            @ApiResponse(responseCode = "400", description = "Valor de nota fuera de rango (0-20)"),
            @ApiResponse(responseCode = "404", description = "Estudiante o Curso no encontrado")
    })
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

    @Operation(summary = "Obtener notas de un estudiante", description = "Retorna la lista de todas las calificaciones pertenecientes a un alumno específico.")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Grade>> getGradesByStudent(
            @Parameter(description = "ID único del estudiante", example = "1") @PathVariable Long studentId) {
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
@Schema(description = "Objeto de transferencia para el registro de calificaciones")
class GradeRequest {
    @Schema(description = "ID del estudiante", example = "4")
    private Long studentId;
    @Schema(description = "ID del curso", example = "1")
    private Long courseId;
    @Schema(description = "Valor numérico de la calificación (0-20)", example = "18.5")
    private Double value;
}