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
}
