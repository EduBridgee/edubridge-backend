package com.upc.edubridge.notification.repository;

import com.upc.edubridge.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByStudentIdAndIsReadFalseOrderByCreatedAtDesc(Long studentId);
}