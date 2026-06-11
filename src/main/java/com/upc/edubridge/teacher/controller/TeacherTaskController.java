package com.upc.edubridge.teacher.controller;

import com.upc.edubridge.teacher.model.*;
import com.upc.edubridge.teacher.repository.TeacherTaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teacher-tasks")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Tareas del Docente", description = "Endpoints para la gestión de recordatorios y actividades pendientes de los profesores")
public class TeacherTaskController {

    @Autowired
    private TeacherTaskRepository teacherTaskRepository;

    @Operation(
            summary = "Listar tareas del servidor",
            description = "Retorna la lista de actividades y pendientes que el docente debe atender, tales como corregir ensayos o resolver ejercicios."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tareas recuperadas exitosamente",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = TeacherTask.class))
                            )
                    }
            ),
            @ApiResponse(responseCode = "500", description = "Error al conectar con la base de datos", content = @Content)
    })
    @GetMapping
    public List<TeacherTask> getAll() {
        return teacherTaskRepository.findAll();
    }
}