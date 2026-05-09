package com.upc.edubridge.student.controller;

import com.upc.edubridge.student.model.Student;
import com.upc.edubridge.student.repository.StudentRepository;
import com.upc.edubridge.student.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "http://localhost:4200")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping
    public List<Student> getAllStudents() {
        return studentService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        return studentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student studentDetails) {
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
    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        if (student.getAverageGrade() == null) student.setAverageGrade(0.0);
        if (student.getRiskLevel() == null) student.setRiskLevel("Bajo");

        return studentRepository.save(student);
    }
}