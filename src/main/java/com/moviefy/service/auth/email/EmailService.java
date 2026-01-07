package com.moviefy.service.auth.email;

import com.moviefy.database.model.entity.user.AppUser;
import com.moviefy.database.model.entity.user.EmailVerificationToken;
import com.moviefy.database.model.entity.user.PasswordResetToken;

import java.util.Optional;

public interface EmailService {
    String generateEmailVerificationToken(AppUser user);

    void sendVerificationEmail(String to, String token);

    void deleteEmailVerificationTokens(EmailVerificationToken emailVerificationToken);

    Optional<EmailVerificationToken> findEmailVerificationToken(String token);

    String generatePasswordResetToken(AppUser user);

    void sendPasswordResetEmail(String to, String token);

    Optional<PasswordResetToken> findPasswordResetToken(String token);

    void deletePasswordResetToken(PasswordResetToken passwordResetToken);
}
