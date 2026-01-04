package com.moviefy.service.auth.email;

import com.moviefy.database.model.entity.user.AppUser;
import com.moviefy.database.model.entity.user.EmailVerificationToken;
import com.moviefy.database.model.entity.user.PasswordResetToken;
import com.moviefy.database.repository.user.EmailVerificationTokenRepository;
import com.moviefy.database.repository.user.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${mail.from}")
    private String from;

    @Value("${moviefy.frontend.url}")
    private String frontendUrl;

    public EmailServiceImpl(JavaMailSender mailSender,
                            EmailVerificationTokenRepository emailVerificationTokenRepository,
                            PasswordResetTokenRepository passwordResetTokenRepository) {
        this.mailSender = mailSender;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        String link = this.frontendUrl + "/verify-email?token=" + token;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(this.from);
        msg.setTo(to);
        msg.setSubject("Verify your Moviefy account");
        msg.setText("Click to verify your email:\n\n" + link + "\n\nIf you didn't request this, ignore this email.");

        this.mailSender.send(msg);
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
    public Optional<EmailVerificationToken> findValidEmailVerificationToken(String token) {
        return this.emailVerificationTokenRepository.findByTokenHash(hashToken(token));
    }

    @Override
    public void deleteEmailVerificationTokens(EmailVerificationToken emailVerificationToken) {
        this.emailVerificationTokenRepository.delete(emailVerificationToken);
    }

    @Override
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

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(this.from);
        msg.setTo(to);
        msg.setSubject("Reset your password");
        msg.setText("Click to reset your password:\n\n" + link + "\n\nIf you didn't request this, ignore this email.");

        this.mailSender.send(msg);
    }

    @Override
    public Optional<PasswordResetToken> findValidPasswordResetToken(String token) {
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
}
