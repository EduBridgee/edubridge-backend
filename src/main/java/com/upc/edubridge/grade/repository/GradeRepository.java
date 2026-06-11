package com.upc.edubridge.grade.repository;

import com.upc.edubridge.grade.model.Grade;
import com.upc.edubridge.grade.model.EvaluationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByStudentId(Long studentId);

    List<Grade> findByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<Grade> findByStudentIdAndCourseIdAndType(Long studentId, Long courseId, EvaluationType type);
}