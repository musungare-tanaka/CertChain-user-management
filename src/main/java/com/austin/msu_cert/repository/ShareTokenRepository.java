package com.austin.msu_cert.repository;

import com.austin.msu_cert.entity.ShareToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShareTokenRepository extends JpaRepository<ShareToken, String> {
    Optional<ShareToken> findByTokenAndActiveTrue(String token);
    List<ShareToken> findByCertIdAndStudentUserId(String certId, String studentUserId);
}