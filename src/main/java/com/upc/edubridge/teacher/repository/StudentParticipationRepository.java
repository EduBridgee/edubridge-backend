package com.upc.edubridge.teacher.repository;

import com.upc.edubridge.teacher.model.StudentParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentParticipationRepository extends JpaRepository<StudentParticipation, Long> {
}