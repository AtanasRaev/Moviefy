package com.moviefy.database.repository;

import com.moviefy.database.model.entity.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    boolean existsByEmail(String email);

    Optional<AppUser> findByEmail(String email);

    @Query("""
                SELECT u FROM AppUser u
                WHERE u.emailVerified = false
                AND u.createdAt <= :threshold
                AND NOT EXISTS (
                    SELECT t FROM EmailVerificationToken t
                    WHERE t.user = u
                    AND t.expiresAt > :now
                )
            """)
    List<AppUser> findUnverifiedUsersWithoutValidTokens(@Param("threshold") LocalDateTime threshold, @Param("now") LocalDateTime now);
}
