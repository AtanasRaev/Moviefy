package com.moviefy.database.repository;

import com.moviefy.database.model.entity.user.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    @Query("""
                SELECT e
                FROM EmailVerificationToken e
                WHERE e.token = :token
            """)
    Optional<EmailVerificationToken> findByToken(@Param("token") String token);
}
