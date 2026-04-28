package com.austin.msu_cert.service;

import com.austin.msu_cert.dto.CertificateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailNotificationService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.notifications.email-enabled:false}")
    private boolean emailEnabled;

    @Value("${app.notifications.from:no-reply@msu.local}")
    private String fromAddress;

    @Value("${app.notifications.fail-on-error:false}")
    private boolean failOnError;

    public EmailNotificationService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public void sendStudentAccountCreated(String to, String fullName, String studentId) {
        String subject = "MSU CERT: Student Account Created";
        String body = """
                Hello %s,

                Your student account has been created successfully.
                Student ID: %s

                You can now sign in and access your certificate dashboard.
                """.formatted(defaultName(fullName), safe(studentId));
        sendEmail(to, subject, body);
    }

    public void sendInstitutionAccountCreated(String to, String institutionName, String registrationNumber) {
        String subject = "MSU CERT: Institution Registration Received";
        String body = """
                Hello %s,

                Your institution account registration has been received.
                Registration Number: %s
                Status: PENDING ADMIN VERIFICATION

                You will receive another email once your account is approved.
                """.formatted(defaultName(institutionName), safe(registrationNumber));
        sendEmail(to, subject, body);
    }

    public void sendInstitutionVerificationStatus(String to, String institutionName, String status) {
        String subject = "MSU CERT: Institution Verification Update";
        String body = """
                Hello %s,

                Your institution account verification status has been updated.
                New Status: %s

                If you believe this is an error, please contact the MSU administrator.
                """.formatted(defaultName(institutionName), safe(status));
        sendEmail(to, subject, body);
    }

    public void sendCertificateIssuedToStudent(String to, String studentName, CertificateResponse certificate) {
        String subject = "MSU CERT: New Certificate Issued";
        String body = """
                Hello %s,

                A new academic certificate has been issued to your account.
                Certificate ID: %s
                Course: %s
                Institution: %s
                Status: %s
                """.formatted(
                defaultName(studentName),
                safe(certificate.getCertId()),
                safe(certificate.getCourseName()),
                safe(certificate.getInstitutionName()),
                safe(certificate.getStatus())
        );
        sendEmail(to, subject, body);
    }

    public void sendCertificateIssuedToInstitution(String to, String institutionName, CertificateResponse certificate) {
        String subject = "MSU CERT: Certificate Issued";
        String body = """
                Hello %s,

                A certificate was successfully issued.
                Certificate ID: %s
                Student ID: %s
                Student Name: %s
                Course: %s
                Status: %s
                """.formatted(
                defaultName(institutionName),
                safe(certificate.getCertId()),
                safe(certificate.getStudentId()),
                safe(certificate.getStudentName()),
                safe(certificate.getCourseName()),
                safe(certificate.getStatus())
        );
        sendEmail(to, subject, body);
    }

    public void sendCertificateRevoked(String to, String recipientName, CertificateResponse certificate) {
        String subject = "MSU CERT: Certificate Revoked";
        String body = """
                Hello %s,

                A certificate has been revoked.
                Certificate ID: %s
                Student ID: %s
                Course: %s
                Current Status: %s
                """.formatted(
                defaultName(recipientName),
                safe(certificate.getCertId()),
                safe(certificate.getStudentId()),
                safe(certificate.getCourseName()),
                safe(certificate.getStatus())
        );
        sendEmail(to, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            return;
        }

        if (!emailEnabled) {
            log.info("Email notifications are disabled. Skipping email to {} with subject '{}'", to, subject);
            return;
        }

        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            String message = "JavaMailSender is not available. Unable to send notification email.";
            handleFailure(message, null);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromAddress);
            }
            sender.send(message);
            log.info("Sent notification email to {} with subject '{}'", to, subject);
        } catch (Exception ex) {
            handleFailure("Failed to send email notification to " + to + " with subject '" + subject + "'", ex);
        }
    }

    private void handleFailure(String message, Exception ex) {
        if (failOnError) {
            throw new IllegalStateException(message, ex);
        }
        if (ex == null) {
            log.warn(message);
        } else {
            log.warn("{}: {}", message, ex.getMessage());
        }
    }

    private String safe(String value) {
        return value == null ? "N/A" : value;
    }

    private String defaultName(String value) {
        if (value == null || value.isBlank()) {
            return "User";
        }
        return value;
    }
}
