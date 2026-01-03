package com.moviefy.service.auth.email;

import com.moviefy.database.model.entity.user.AppUser;
import com.moviefy.database.model.entity.user.EmailVerificationToken;
import com.moviefy.database.repository.EmailVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final EmailVerificationTokenRepository tokenRepository;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${mail.from}")
    private String from;

    @Value("${moviefy.frontend.url}")
    private String frontendUrl;

    public EmailServiceImpl(JavaMailSender mailSender,
                            EmailVerificationTokenRepository tokenRepository) {
        this.mailSender = mailSender;
        this.tokenRepository = tokenRepository;
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
    public String generateToken(AppUser user) {
        String token = this.tokenGenerator();

        EmailVerificationToken evt = new EmailVerificationToken();
        evt.setToken(token);
        evt.setUser(user);
        evt.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        this.tokenRepository.save(evt);

        return token;
    }

    @Override
    public Optional<EmailVerificationToken> findValidToken(String token) {
        return this.tokenRepository.findByToken(token);
    }

    @Override
    public void delete(EmailVerificationToken emailVerificationToken) {
        this.tokenRepository.delete(emailVerificationToken);
    }

    private String tokenGenerator() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
