package com.upc.edubridge.course.controller;

import com.upc.edubridge.course.model.Course;
import com.upc.edubridge.course.repository.CourseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Cursos", description = "Endpoints para la gestión del catálogo de cursos de EduBridge")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Operation(
            summary = "Listar todos los cursos",
            description = "Obtiene la lista completa de cursos disponibles en la plataforma, incluyendo créditos y categorías."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de cursos obtenida exitosamente",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = Course.class))
                            )
                    }
            ),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @GetMapping
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }
}