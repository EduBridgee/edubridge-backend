package com.upc.edubridge.support.repository;

import com.upc.edubridge.support.model.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {
}
