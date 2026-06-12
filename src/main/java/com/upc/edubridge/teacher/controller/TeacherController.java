package com.upc.edubridge.teacher.controller;

import com.upc.edubridge.teacher.model.Teacher;
import com.upc.edubridge.teacher.repository.TeacherRepository;
import com.upc.edubridge.course.repository.CourseRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/teachers")
@Tag(name = "Profesores", description = "Endpoints para la gestión de docentes en EduBridge")
public class TeacherController {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(
            summary = "Listar todos los profesores",
            description = "Retorna una lista completa de todos los docentes registrados en el sistema."
    )
    @GetMapping
    public List<Teacher> getAll() {
        return teacherRepository.findAll();
    }

    @Operation(
            summary = "Registrar un nuevo profesor",
            description = "Crea un nuevo registro de docente y lo vincula opcionalmente a un curso."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Profesor creado exitosamente",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Teacher.class)) }),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<Teacher> create(@RequestBody Teacher teacher, @RequestParam(required = false) Long courseId) {
        if (teacher.getPassword() != null && !teacher.getPassword().isEmpty()) {
            teacher.setPassword(passwordEncoder.encode(teacher.getPassword()));
        }
        if (courseId != null) {
            courseRepository.findById(courseId).ifPresent(teacher::setCourse);
        } else if (teacher.getCourse() != null && teacher.getCourse().getId() != null) {
            courseRepository.findById(teacher.getCourse().getId()).ifPresent(teacher::setCourse);
        }
        Teacher savedTeacher = teacherRepository.save(teacher);
        if (savedTeacher.getCourse() != null) {
            com.upc.edubridge.course.model.Course course = savedTeacher.getCourse();
            course.setTeacher(savedTeacher);
            courseRepository.save(course);
        }
        return new ResponseEntity<>(savedTeacher, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Teacher> update(
            @PathVariable Long id,
            @RequestBody Teacher teacherDetails,
            @RequestParam(required = false) Long courseId) {
        return teacherRepository.findById(id).map(teacher -> {
            teacher.setName(teacherDetails.getName());
            teacher.setEmail(teacherDetails.getEmail());
            teacher.setSpecialization(teacherDetails.getSpecialization());
            if (teacherDetails.getPassword() != null && !teacherDetails.getPassword().isEmpty()) {
                teacher.setPassword(passwordEncoder.encode(teacherDetails.getPassword()));
            }

            if (teacher.getCourse() != null) {
                com.upc.edubridge.course.model.Course oldCourse = teacher.getCourse();
                oldCourse.setTeacher(null);
                courseRepository.save(oldCourse);
            }

            if (courseId != null) {
                courseRepository.findById(courseId).ifPresent(teacher::setCourse);
            } else if (teacherDetails.getCourse() != null && teacherDetails.getCourse().getId() != null) {
                courseRepository.findById(teacherDetails.getCourse().getId()).ifPresent(teacher::setCourse);
            } else {
                teacher.setCourse(null);
            }

            Teacher updatedTeacher = teacherRepository.save(teacher);

            if (updatedTeacher.getCourse() != null) {
                com.upc.edubridge.course.model.Course course = updatedTeacher.getCourse();
                course.setTeacher(updatedTeacher);
                courseRepository.save(course);
            }

            return ResponseEntity.ok(updatedTeacher);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeacher(@PathVariable Long id) {
        return teacherRepository.findById(id).map(teacher -> {
            if (teacher.getCourse() != null) {
                com.upc.edubridge.course.model.Course course = teacher.getCourse();
                course.setTeacher(null);
                courseRepository.save(course);
            }
            courseRepository.findAll().forEach(course -> {
                if (course.getTeacher() != null && course.getTeacher().getId().equals(teacher.getId())) {
                    course.setTeacher(null);
                    courseRepository.save(course);
                }
            });
            teacherRepository.delete(teacher);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}