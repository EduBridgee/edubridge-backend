package com.upc.edubridge.student.controller;

import com.upc.edubridge.student.dto.StudentTaskRequest;
import com.upc.edubridge.student.model.Student;
import com.upc.edubridge.student.model.StudentTask;
import com.upc.edubridge.student.repository.StudentRepository;
import com.upc.edubridge.student.repository.StudentTaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-tasks")
@Tag(name = "Tareas de Estudiantes", description = "Endpoints para la gestión de tareas asignadas a los alumnos")
public class StudentTaskController {

    @Autowired
    private StudentTaskRepository studentTaskRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private com.upc.edubridge.grade.repository.GradeRepository gradeRepository;

    @Autowired
    private com.upc.edubridge.course.repository.CourseRepository courseRepository;

    @Operation(
            summary = "Obtener tareas por ID de estudiante",
            description = "Retorna la lista de tareas asignadas a un estudiante específico mediante su ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tareas obtenida exitosamente",
                    content = { @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = StudentTask.class))) })
    })
    @GetMapping("/student/{studentId}")
    public List<StudentTask> getTasksByStudentId(@PathVariable Long studentId) {
        return studentTaskRepository.findByStudentId(studentId);
    }

    @Operation(
            summary = "Asignar nueva tarea a un estudiante",
            description = "Crea y asigna una nueva tarea a un estudiante específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarea asignada exitosamente",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StudentTask.class)) }),
            @ApiResponse(responseCode = "404", description = "Estudiante no encontrado")
    })
    @PostMapping
    public ResponseEntity<StudentTask> createStudentTask(@RequestBody StudentTaskRequest request) {
        return studentRepository.findById(request.getStudentId())
                .map(student -> {
                    StudentTask task = StudentTask.builder()
                            .title(request.getTitle())
                            .courseName(request.getCourseName())
                            .dueDate(request.getDueDate())
                            .status("Pendiente")
                            .student(student)
                            .teacherName(request.getTeacherName())
                            .build();
                    StudentTask savedTask = studentTaskRepository.save(task);
                    return ResponseEntity.ok(savedTask);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Obtener todas las tareas de estudiantes",
            description = "Retorna la lista de todas las tareas asignadas a estudiantes en el sistema"
    )
    @GetMapping
    public List<StudentTask> getAllTasks() {
        return studentTaskRepository.findAll();
    }

    @Operation(
            summary = "Actualizar estado de una tarea (Entregar/Completar)",
            description = "Marca una tarea como entregada. Si la fecha actual supera la fecha de vencimiento (YYYY-MM-DD), se marca como 'Atrasada', de lo contrario como 'Entregada'."
    )
    @PutMapping("/{id}/submit")
    public ResponseEntity<StudentTask> submitTask(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        return studentTaskRepository.findById(id)
                .map(task -> {
                    task.setSubmissionType(payload.get("submissionType"));
                    task.setSubmissionFileName(payload.get("submissionFileName"));
                    task.setSubmissionContent(payload.get("submissionContent"));
                    task.setStudentComment(payload.get("studentComment"));
                    try {
                        java.time.LocalDate due = java.time.LocalDate.parse(task.getDueDate());
                        java.time.LocalDate now = java.time.LocalDate.now();
                        if (now.isAfter(due)) {
                            task.setStatus("Atrasado");
                        } else {
                            task.setStatus("Entregado");
                        }
                    } catch (Exception e) {
                        task.setStatus("Entregado");
                    }
                    StudentTask updatedTask = studentTaskRepository.save(task);
                    return ResponseEntity.ok(updatedTask);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Calificar y actualizar estado de una tarea",
            description = "Permite al docente registrar la calificación, cambia el estado a 'Calificado' y recalcula la nota de evaluación de desempeño (DD) como promedio de todas sus tareas."
    )
    @PutMapping("/{id}/grade")
    public ResponseEntity<StudentTask> gradeTask(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        return studentTaskRepository.findById(id)
                .map(task -> {
                    try {
                        Double scoreValue = Double.parseDouble(payload.get("score"));
                        task.setScore(scoreValue);
                    } catch (Exception e) {
                        task.setScore(0.0);
                    }
                    task.setStatus("Calificado");
                    StudentTask updatedTask = studentTaskRepository.save(task);

                    Student student = task.getStudent();
                    if (student != null) {
                        List<StudentTask> studentTasks = studentTaskRepository.findByStudentId(student.getId());
                        double averageScore = studentTasks.stream()
                                .filter(t -> t.getCourseName() != null && t.getCourseName().equalsIgnoreCase(task.getCourseName()))
                                .filter(t -> "Calificado".equalsIgnoreCase(t.getStatus()) && t.getScore() != null)
                                .mapToDouble(StudentTask::getScore)
                                .average()
                                .orElse(0.0);

                        com.upc.edubridge.course.model.Course course = courseRepository.findAll().stream()
                                .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(task.getCourseName()))
                                .findFirst()
                                .orElse(null);

                        if (course != null) {
                            java.util.Optional<com.upc.edubridge.grade.model.Grade> existingGrade = gradeRepository.findByStudentIdAndCourseIdAndType(
                                    student.getId(), course.getId(), com.upc.edubridge.grade.model.EvaluationType.DD);

                            com.upc.edubridge.grade.model.Grade grade;
                            if (existingGrade.isPresent()) {
                                grade = existingGrade.get();
                                grade.setValue(averageScore);
                            } else {
                                grade = com.upc.edubridge.grade.model.Grade.builder()
                                        .value(averageScore)
                                        .type(com.upc.edubridge.grade.model.EvaluationType.DD)
                                        .student(student)
                                        .course(course)
                                        .build();
                            }
                            gradeRepository.save(grade);

                            actualizarPromedioEstudiante(student);
                        }
                    }

                    return ResponseEntity.ok(updatedTask);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private void actualizarPromedioEstudiante(Student student) {
        List<com.upc.edubridge.grade.model.Grade> notas = gradeRepository.findByStudentId(student.getId());
        if (notas != null && !notas.isEmpty()) {
            double promedio = notas.stream()
                    .filter(g -> g.getValue() != null)
                    .mapToDouble(com.upc.edubridge.grade.model.Grade::getValue)
                    .average()
                    .orElse(0.0);
            student.setAverageGrade(Math.round(promedio * 10.0) / 10.0);
            studentRepository.save(student);
        }
    }
}
