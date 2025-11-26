package com.namdang.memos.service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Year;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MailService {
    // spring auto create JavaMailSender since we config it in yaml + starter.mail
    final JavaMailSender mailSender;

    @Value("${memos.mail.from}")
    private String from;

    @Value("${memos.app-url}")
    private String appUrl;

    private String projectInviteTemplate;

    @PostConstruct
    void loadTemplates() {
        try {
            var resource = new ClassPathResource("templates/project-invite.html");
            byte[] bytes = resource.getInputStream().readAllBytes();
            projectInviteTemplate = new String(bytes, StandardCharsets.UTF_8);
            log.info("Loaded project-invite email template");
        } catch (IOException e) {
            log.error("Failed to load project-invite.html template", e);
            projectInviteTemplate = null;
        }
    }

    public void sendProjectInviteMail(
            String to,
            String projectName,
            String inviterName,
            String inviteToken
    ) {
        String subject = "[Memos] Invitation to project \"" + projectName + "\"";
        String inviteLink = appUrl + "/invite?token=" + inviteToken;
        String html = buildInviteHtml(projectName, inviterName, inviteLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Sent invite email to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send invite email to {}", to, e);
        }
    }

    private String buildInviteHtml(String projectName, String inviterName, String inviteLink) {
        String tmpl = projectInviteTemplate;
        if (tmpl == null) {
            return """
                    <p>%s invited you to project <b>%s</b> in Memos.</p>
                    <p>Link: <a href="%s">%s</a></p>
                    """.formatted(escapeHtml(inviterName), escapeHtml(projectName), inviteLink, inviteLink);
        }

        return tmpl
                .replace("{{PROJECT_NAME}}", escapeHtml(projectName))
                .replace("{{INVITER_NAME}}", escapeHtml(inviterName))
                .replace("{{INVITE_LINK}}", inviteLink)
                .replace("{{CURRENT_YEAR}}", String.valueOf(Year.now().getValue()));
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}

