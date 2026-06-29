package com.upc.edubridge.student.repository;

import com.upc.edubridge.student.model.StudentTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentTaskRepository extends JpaRepository<StudentTask, Long> {
    List<StudentTask> findByStudentId(Long studentId);
}
