package com.upc.edubridge.enrollment.repository;

import com.upc.edubridge.enrollment.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findByCourseId(Long courseId);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Enrollment e SET e.participations = e.participations + :points WHERE e.student.id = :studentId AND e.course.id = :courseId")
    int incrementarPuntosParticipacion(@Param("studentId") Long studentId, @Param("courseId") Long courseId, @Param("points") int points);
}