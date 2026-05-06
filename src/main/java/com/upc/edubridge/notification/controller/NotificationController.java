package com.upc.edubridge.notification.controller;

import com.upc.edubridge.notification.model.Notification;
import com.upc.edubridge.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @PostMapping
    public Notification sendNotification(@RequestBody Notification notification) {
        return notificationRepository.save(notification);
    }

    @GetMapping("/student/{studentId}")
    public List<Notification> getMyNotifications(@PathVariable Long studentId) {
        return notificationRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        return notificationRepository.findById(id).map(notif -> {
            notif.setRead(true);
            notificationRepository.save(notif);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}