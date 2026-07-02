package com.upc.edubridge.resource.repository;
import com.upc.edubridge.resource.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {}