package com.upc.edubridge.teacher.repository;
import com.upc.edubridge.teacher.model.TeacherTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherTaskRepository extends JpaRepository<TeacherTask, Long> {}