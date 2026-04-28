package com.austin.msu_cert.repository;

import com.austin.msu_cert.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop200ByOrderByCreatedAtDesc();
    List<AuditLog> findTop100ByOrderByCreatedAtDesc();
    List<AuditLog> findTop50ByOrderByCreatedAtDesc();
}
