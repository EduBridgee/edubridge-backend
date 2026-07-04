package com.upc.edubridge.support.repository;

import com.upc.edubridge.support.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<SupportTicket> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);
}
