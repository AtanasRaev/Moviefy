package com.moviefy.service.auth.email;

import com.moviefy.database.model.entity.user.AppUser;
import com.moviefy.database.model.entity.user.EmailVerificationToken;

import java.util.Optional;

public interface EmailService {
    void sendVerificationEmail(String to, String token);

    String generateToken(AppUser user);

    Optional<EmailVerificationToken> findValidToken(String token);

    void delete(EmailVerificationToken emailVerificationToken);

    Optional<EmailVerificationToken> findToken(String token);
}
