package com.upc.edubridge.student.controller;

import com.upc.edubridge.student.model.Student;
import com.upc.edubridge.student.repository.StudentRepository;
import com.upc.edubridge.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Estudiantes", description = "Endpoints para la gestión de perfiles estudiantiles y métricas académicas")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Operation(
            summary = "Listar todos los estudiantes",
            description = "Retorna la lista completa de estudiantes registrados con su información académica y de contacto."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida con éxito",
                    content = { @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Student.class))) })
    })
    @GetMapping
    public List<Student> getAllStudents() {
        return studentService.findAll();
    }

    @Operation(
            summary = "Obtener estudiante por ID",
            description = "Busca y retorna el perfil detallado de un estudiante específico mediante su identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estudiante encontrado"),
            @ApiResponse(responseCode = "404", description = "Estudiante no registrado en el sistema")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(
            @Parameter(description = "ID único del estudiante", example = "4") @PathVariable Long id) {
        return studentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Actualizar información del estudiante",
            description = "Permite modificar los datos personales, el nivel de riesgo y las métricas de asistencia/rendimiento de un alumno existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "No se encontró el estudiante para actualizar")
    })
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Student> updateStudent(
            @Parameter(description = "ID del estudiante a modificar") @PathVariable Long id,
            @RequestBody Student studentDetails) {
        return studentRepository.findById(id)
                .map(student -> {
                    student.setName(studentDetails.getName());
                    student.setEmail(studentDetails.getEmail());
                    student.setCode(studentDetails.getCode());
                    student.setProgram(studentDetails.getProgram());
                    student.setStatus(studentDetails.getStatus());
                    student.setSemester(studentDetails.getSemester());
                    student.setPhone(studentDetails.getPhone());
                    student.setAddress(studentDetails.getAddress());
                    student.setRole(studentDetails.getRole());
                    student.setRiskLevel(studentDetails.getRiskLevel());
                    student.setAverageGrade(studentDetails.getAverageGrade());
                    student.setAbsences(studentDetails.getAbsences());
                    student.setTotalClasses(studentDetails.getTotalClasses());
                    student.setAttendedClasses(studentDetails.getAttendedClasses());
                    student.setAttendancePercentage(studentDetails.getAttendancePercentage());

                    Student updatedStudent = studentRepository.save(student);
                    System.out.println(">> EduBridge: Registro de " + student.getName() + " actualizado en DB.");
                    return ResponseEntity.ok(updatedStudent);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Crear nuevo estudiante",
            description = "Registra un nuevo alumno en la plataforma. Si no se especifican, el promedio inicia en 0.0 y el riesgo en 'Bajo'."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Estudiante creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de registro inválidos")
    })
    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        if (student.getAverageGrade() == null) student.setAverageGrade(0.0);
        if (student.getRiskLevel() == null) student.setRiskLevel("Bajo");
        return studentRepository.save(student);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        return studentRepository.findById(id).map(student -> {
            studentRepository.delete(student);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}