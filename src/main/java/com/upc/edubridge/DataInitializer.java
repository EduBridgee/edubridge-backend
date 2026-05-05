package com.upc.edubridge;

import com.upc.edubridge.course.model.Course;
import com.upc.edubridge.course.repository.CourseRepository;
import com.upc.edubridge.resource.repository.ResourceRepository;
import com.upc.edubridge.student.repository.StudentRepository;
import com.upc.edubridge.teacher.repository.TeacherTaskRepository;
import com.upc.edubridge.tutoring.repository.TutoringRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public DataInitializer(CourseRepository courseRepository, StudentRepository studentRepository) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        System.out.println(">> EduBridge: Sincronizando con base de datos real...");

        if (courseRepository.count() == 0) {
            courseRepository.save(Course.builder()
                    .name("Ecuaciones Diferenciales")
                    .code("MA264")
                    .credits(4)
                    .category("Ciencias")
                    .icon("📐")
                    .build());

            courseRepository.save(Course.builder()
                    .name("Arquitectura de Computadoras")
                    .code("CC123")
                    .credits(4)
                    .category("Computación")
                    .icon("💻")
                    .build());

            courseRepository.save(Course.builder()
                    .name("Física II")
                    .code("MA466")
                    .credits(5)
                    .category("Ciencias")
                    .icon("⚡")
                    .build());

            System.out.println(">> EduBridge: Cursos académicos inicializados.");
        }
    }
}