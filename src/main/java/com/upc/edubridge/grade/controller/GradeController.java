package com.upc.edubridge.grade.controller;

import com.upc.edubridge.course.model.Course;
import com.upc.edubridge.course.repository.CourseRepository;
import com.upc.edubridge.grade.model.Grade;
import com.upc.edubridge.grade.model.EvaluationType;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@Tag(name = "Calificaciones", description = "Endpoints para la gestión de notas y cálculo de promedios académicos")
public class GradeController {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Operation(summary = "Listar todas las notas", description = "Retorna un historial completo de todas las calificaciones registradas en el sistema.")
    @ApiResponse(responseCode = "200", description = "Lista de notas obtenida con éxito",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Grade.class))))
    @GetMapping
    public ResponseEntity<List<Grade>> getAllGrades() {
        return ResponseEntity.ok(gradeRepository.findAll());
    }

    @Operation(
            summary = "Registrar o Actualizar calificación",
            description = "Registra una nota específica (PC1, EA, EB, etc.). Si ya existe una nota del mismo tipo para el alumno en ese curso, el sistema la actualiza automáticamente y recalcula el promedio general del estudiante."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Nota procesada y promedio actualizado",
                    content = @Content(schema = @Schema(implementation = Grade.class))),
            @ApiResponse(responseCode = "400", description = "Error de validación (ej. nota fuera del rango 0-20)"),
            @ApiResponse(responseCode = "404", description = "El Estudiante o el Curso especificado no existen")
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

        Optional<Grade> existingGrade = gradeRepository.findByStudentIdAndCourseIdAndType(
                request.getStudentId(), request.getCourseId(), request.getType());

        Grade grade;
        if (existingGrade.isPresent()) {
            grade = existingGrade.get();
            grade.setValue(request.getValue());
        } else {
            grade = Grade.builder()
                    .value(request.getValue())
                    .type(request.getType())
                    .student(student)
                    .course(course)
                    .build();
        }

        gradeRepository.save(grade);
        actualizarPromedioEstudiante(student);

        return ResponseEntity.status(HttpStatus.CREATED).body(grade);
    }

    @Operation(summary = "Obtener notas de un estudiante", description = "Retorna todas las calificaciones de un alumno en todos sus cursos.")
    @ApiResponse(responseCode = "200", description = "Notas encontradas")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Grade>> getGradesByStudent(
            @Parameter(description = "ID único del estudiante", example = "4") @PathVariable Long studentId) {
        return ResponseEntity.ok(gradeRepository.findByStudentId(studentId));
    }

    @Operation(summary = "Obtener notas de un estudiante por curso", description = "Retorna el detalle de notas (PC1, PC2, etc.) de un alumno en una materia específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalle de curso obtenido"),
            @ApiResponse(responseCode = "404", description = "No se encontraron notas para esa combinación")
    })
    @GetMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<List<Grade>> getGradesByCourse(
            @Parameter(description = "ID del estudiante") @PathVariable Long studentId,
            @Parameter(description = "ID del curso") @PathVariable Long courseId) {
        return ResponseEntity.ok(gradeRepository.findByStudentIdAndCourseId(studentId, courseId));
    }

    private void actualizarPromedioEstudiante(Student student) {
        List<Grade> notas = gradeRepository.findByStudentId(student.getId());
        if (notas != null && !notas.isEmpty()) {
            double promedio = notas.stream()
                    .filter(g -> g.getValue() != null)
                    .mapToDouble(Grade::getValue)
                    .average()
                    .orElse(0.0);
            student.setAverageGrade(Math.round(promedio * 10.0) / 10.0);
            studentRepository.save(student);
        }
    }
}

@Data
@Schema(description = "Objeto necesario para registrar o actualizar una calificación")
class GradeRequest {
    @Schema(description = "ID del estudiante al que se le asigna la nota", example = "4")
    private Long studentId;

    @Schema(description = "ID del curso correspondiente", example = "6")
    private Long courseId;

    @Schema(description = "Valor numérico de la calificación (0-20)", example = "18.5")
    private Double value;

    @Schema(description = "Tipo de evaluación académica", example = "PC1")
    private EvaluationType type;
}