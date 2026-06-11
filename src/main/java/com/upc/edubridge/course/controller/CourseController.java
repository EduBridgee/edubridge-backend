package com.upc.edubridge.course.controller;

import com.upc.edubridge.course.model.Course;
import com.upc.edubridge.course.repository.CourseRepository;
import com.upc.edubridge.teacher.repository.TeacherRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Cursos", description = "Endpoints para la gestión del catálogo de cursos de EduBridge")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

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

    @Operation(
            summary = "Crear un nuevo curso",
            description = "Registra un nuevo curso en el sistema y opcionalmente lo vincula a un docente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Curso creado exitosamente",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Course.class)) }),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course, @RequestParam(required = false) Long teacherId) {
        if (teacherId != null) {
            teacherRepository.findById(teacherId).ifPresent(course::setTeacher);
        }
        Course savedCourse = courseRepository.save(course);
        return new ResponseEntity<>(savedCourse, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Actualizar un curso existente",
            description = "Actualiza los detalles de un curso y su asignación de docente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Curso actualizado exitosamente",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Course.class)) }),
            @ApiResponse(responseCode = "404", description = "Curso no encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @RequestBody Course courseDetails,
            @RequestParam(required = false) Long teacherId) {
        return courseRepository.findById(id).map(course -> {
            course.setName(courseDetails.getName());
            course.setCode(courseDetails.getCode());
            course.setCredits(courseDetails.getCredits());
            course.setCategory(courseDetails.getCategory());
            course.setIcon(courseDetails.getIcon());
            if (teacherId != null) {
                teacherRepository.findById(teacherId).ifPresent(course::setTeacher);
            } else {
                course.setTeacher(null);
            }
            Course updatedCourse = courseRepository.save(course);
            return new ResponseEntity<>(updatedCourse, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}