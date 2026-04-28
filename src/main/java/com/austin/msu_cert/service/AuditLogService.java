package com.austin.msu_cert.service;

import com.austin.msu_cert.dto.AuditLogResponse;
import com.austin.msu_cert.entity.AuditLog;
import com.austin.msu_cert.entity.User;
import com.austin.msu_cert.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(
            String actorEmail,
            String actorRole,
            String action,
            String targetType,
            String targetId,
            String details
    ) {
        AuditLog log = AuditLog.builder()
                .actorEmail(actorEmail)
                .actorRole(actorRole)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    @Transactional
    public void log(User actor, String action, String targetType, String targetId, String details) {
        log(
                actor.getEmail(),
                actor.getRole().name(),
                action,
                targetType,
                targetId,
                details
        );
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getRecent(int limit) {
        int safeLimit = Math.max(0, Math.min(limit, 200));
        return auditLogRepository.findTop200ByOrderByCreatedAtDesc()
                .stream()
                .limit(safeLimit)
                .map(AuditLogResponse::from)
                .toList();
    }
}
