package com.moviefy.service.auth.email;

import com.moviefy.database.model.entity.user.AppUser;
import com.moviefy.database.model.entity.user.EmailVerificationToken;
import com.moviefy.database.model.entity.user.PasswordResetToken;
import com.moviefy.database.repository.user.EmailVerificationTokenRepository;
import com.moviefy.database.repository.user.PasswordResetTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ResourceLoader resourceLoader;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${mail.from}")
    private String from;

    @Value("${moviefy.frontend.url}")
    private String frontendUrl;

    @Value("${moviefy.logo.url}")
    private String logoUrl;

    public EmailServiceImpl(JavaMailSender mailSender,
                            EmailVerificationTokenRepository emailVerificationTokenRepository,
                            PasswordResetTokenRepository passwordResetTokenRepository,
                            ResourceLoader resourceLoader) {
        this.mailSender = mailSender;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        String link = this.frontendUrl + "/verify-email?token=" + token;

        try {
            MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String htmlContent = loadTemplate("classpath:templates/email-verification.html");
            htmlContent = htmlContent.replace("{{MOVIEFY_LOGO_URL}}", this.logoUrl);
            htmlContent = htmlContent.replace("{{VERIFY_URL}}", link);

            helper.setFrom(this.from);
            helper.setTo(to);
            helper.setSubject("Verify your Moviefy account");
            helper.setText(htmlContent, true);

            this.mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public String generateEmailVerificationToken(AppUser user) {
        String token = this.tokenGenerator();

        EmailVerificationToken evt = new EmailVerificationToken();
        evt.setTokenHash(hashToken(token));
        evt.setUser(user);
        evt.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        this.emailVerificationTokenRepository.save(evt);

        return token;
    }

    @Override
    public void deleteEmailVerificationTokens(EmailVerificationToken emailVerificationToken) {
        this.emailVerificationTokenRepository.delete(emailVerificationToken);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmailVerificationToken> findEmailVerificationToken(String token) {
        return this.emailVerificationTokenRepository.findByTokenHash(hashToken(token));
    }

    @Override
    public String generatePasswordResetToken(AppUser user) {
        String token = this.tokenGenerator();

        PasswordResetToken evt = new PasswordResetToken();
        evt.setTokenHash(hashToken(token));
        evt.setUser(user);
        evt.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        this.passwordResetTokenRepository.save(evt);

        return token;
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String link = this.frontendUrl + "/password-reset?token=" + token;

        try {
            MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String htmlContent = loadTemplate("classpath:templates/reset-password.html");
            htmlContent = htmlContent.replace("{{MOVIEFY_LOGO_URL}}", this.logoUrl);
            htmlContent = htmlContent.replace("{{RESET_URL}}", link);

            helper.setFrom(this.from);
            helper.setTo(to);
            helper.setSubject("Reset your password");
            helper.setText(htmlContent, true);

            this.mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> findPasswordResetToken(String token) {
        return this.passwordResetTokenRepository.findByTokenHash(hashToken(token));
    }

    @Override
    public void deletePasswordResetToken(PasswordResetToken passwordResetToken) {
        this.passwordResetTokenRepository.delete(passwordResetToken);
    }

    private String tokenGenerator() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String loadTemplate(String path) {
        Resource resource = this.resourceLoader.getResource(path);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load email template: " + path, e);
        }
    }
}
