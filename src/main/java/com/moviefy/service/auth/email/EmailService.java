package com.moviefy.service.auth.email;

import com.moviefy.database.model.entity.user.AppUser;
import com.moviefy.database.model.entity.user.EmailVerificationToken;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailService {
    void sendVerificationEmail(String to, String token);

    String generateToken(AppUser user);

    Optional<EmailVerificationToken> findValidToken(String token);

    void delete(EmailVerificationToken emailVerificationToken);

    @Query("SELECT e FROM EmailVerificationToken e WHERE e.token = :token")
    Optional<EmailVerificationToken> findToken(@Param("token") String token);
}
