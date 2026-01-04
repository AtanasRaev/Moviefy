package com.moviefy.service.auth;

import com.moviefy.database.model.dto.databaseDto.user.EmailVerificationTokenDTO;
import com.moviefy.database.model.dto.databaseDto.user.PasswordResetConfirmDTO;
import com.moviefy.database.model.dto.databaseDto.user.PasswordResetRequestDTO;
import com.moviefy.database.model.dto.databaseDto.user.RegisterUserDTO;
import com.moviefy.database.model.entity.user.AppUser;
import com.moviefy.database.model.entity.user.EmailVerificationToken;
import com.moviefy.database.model.entity.user.PasswordResetToken;
import com.moviefy.database.model.entity.user.UserRole;
import com.moviefy.database.repository.user.UserRepository;
import com.moviefy.exceptions.EmailAlreadyTakenException;
import com.moviefy.exceptions.ExpiredPasswordResetTokenException;
import com.moviefy.exceptions.ExpiredVerificationTokenException;
import com.moviefy.exceptions.InvalidTokenException;
import com.moviefy.service.auth.email.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("#{'${moviefy.admin.emails:}'.split(',')}")
    private List<String> adminEmails;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void register(RegisterUserDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();

        if (this.userRepository.existsByEmail(email)) {
            throw new EmailAlreadyTakenException(
                    email + " is already taken. Please choose another email address."
            );
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setFirstName(normalizeName(dto.getFirstName()));
        user.setLastName(normalizeName(dto.getLastName()));
        user.setPasswordHash(this.passwordEncoder.encode(dto.getPassword()));

        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.USER);

        if (isAdminEmail(email)) {
            roles.add(UserRole.ADMIN);
        }
        user.setRoles(roles);
        this.userRepository.save(user);

        String token = this.emailService.generateEmailVerificationToken(user);
        this.emailService.sendVerificationEmail(user.getEmail(), token);
    }

    @Override
    @Transactional
    public void verifyEmail(EmailVerificationTokenDTO emailVerificationTokenDTO) {
        Optional<EmailVerificationToken> validToken = this.emailService.findValidEmailVerificationToken(emailVerificationTokenDTO.getToken());

        if (validToken.isEmpty()) {
            throw new InvalidTokenException("Invalid token.");
        }

        EmailVerificationToken emailVerificationToken = validToken.get();

        if (emailVerificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ExpiredVerificationTokenException("Verification token has expired.");
        }

        AppUser user = emailVerificationToken.getUser();
        user.setEmailVerified(true);

        this.emailService.deleteEmailVerificationTokens(emailVerificationToken);
        this.userRepository.save(user);
    }

    @Override
    @Transactional
    public void resendEmailVerification(EmailVerificationTokenDTO emailVerificationTokenDTO) {
        Optional<EmailVerificationToken> optional = this.emailService.findEmailVerificationToken(emailVerificationTokenDTO.getToken());

        if (optional.isEmpty()) {
            throw new InvalidTokenException("Invalid optional.");
        }

        EmailVerificationToken emailVerificationToken = optional.get();
        String email = emailVerificationToken.getUser().getEmail();

        String token = this.emailService.generateEmailVerificationToken(emailVerificationToken.getUser());
        this.emailService.sendVerificationEmail(email, token);
        this.emailService.deleteEmailVerificationTokens(emailVerificationToken);
    }

    @Override
    public void requestPasswordReset(PasswordResetRequestDTO passwordResetRequestDTO) {
        Optional<AppUser> byEmail = this.userRepository.findByEmail(passwordResetRequestDTO.getEmail());

        if (byEmail.isEmpty()) {
            return;
        }
        AppUser user = byEmail.get();

        String token = this.emailService.generatePasswordResetToken(user);
        this.emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Override
    public void confirmPasswordReset(PasswordResetConfirmDTO passwordResetConfirmDTO) {
        Optional<PasswordResetToken> validToken = this.emailService.findValidPasswordResetToken(passwordResetConfirmDTO.getToken());

        if (validToken.isEmpty()) {
            throw new InvalidTokenException("Invalid token.");
        }

        PasswordResetToken passwordResetToken = validToken.get();

        if (passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ExpiredPasswordResetTokenException("Password reset token has expired.");
        }

        AppUser user = passwordResetToken.getUser();
        user.setPasswordHash(this.passwordEncoder.encode(passwordResetConfirmDTO.getPassword()));

        this.emailService.deletePasswordResetToken(passwordResetToken);
        this.userRepository.save(user);
    }

    private static String normalizeName(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private boolean isAdminEmail(String email) {
        if (this.adminEmails == null) {
            return false;
        }

        String normalized = email.trim().toLowerCase();

        return this.adminEmails.stream()
                .map(s -> s == null ? "" : s.trim().toLowerCase())
                .anyMatch(s -> !s.isBlank() && s.equals(normalized));
    }
}
