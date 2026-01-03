package com.moviefy.database.repository;

import com.moviefy.database.model.entity.user.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    @Query("""
                SELECT e
                FROM EmailVerificationToken e
                WHERE e.token = :token
            """)
    Optional<EmailVerificationToken> findByToken(@Param("token") String token);

    @Modifying
    @Query("""
                DELETE FROM EmailVerificationToken em
                WHERE em.expiresAt < :now
            """)
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

}
