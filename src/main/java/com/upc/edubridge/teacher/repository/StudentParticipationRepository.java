package com.upc.edubridge.teacher.repository;

import com.upc.edubridge.teacher.model.StudentParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentParticipationRepository extends JpaRepository<StudentParticipation, Long> {

    List<StudentParticipation> findByStudentIdOrderByRegistrationDateDesc(Long studentId);
}