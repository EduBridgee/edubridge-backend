package com.upc.edubridge.auth.repository;

import com.upc.edubridge.auth.model.User2faConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface User2faConfigRepository extends JpaRepository<User2faConfig, String> {
    Optional<User2faConfig> findByEmail(String email);
}
