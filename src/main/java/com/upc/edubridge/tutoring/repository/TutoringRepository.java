package com.upc.edubridge.tutoring.repository;

import com.upc.edubridge.tutoring.model.TutoringSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutoringRepository extends JpaRepository<TutoringSession, Long> {
}