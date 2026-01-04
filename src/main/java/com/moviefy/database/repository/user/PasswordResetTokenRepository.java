package com.moviefy.database.repository.user;

import com.moviefy.database.model.entity.user.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Query("""
                SELECT p
                FROM PasswordResetToken p
                WHERE p.tokenHash = :tokenHash
            """)
    Optional<PasswordResetToken> findByTokenHash(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("""
                DELETE FROM PasswordResetToken t
                WHERE t.expiresAt <= :now
            """)
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
}
