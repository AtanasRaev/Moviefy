package com.moviefy.database.repository.user;

import com.moviefy.database.model.entity.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Query("select u.id from AppUser u where u.email = :email")
    Long findIdByEmail(@Param("email") String email);

    @Query(value = "SELECT movie_id FROM user_favorite_movies WHERE user_id = :userId", nativeQuery = true)
    Set<Long> findFavoriteMovieIds(@Param("userId") long userId);

    @Query(value = "SELECT tv_id FROM user_favorite_series WHERE user_id = :userId", nativeQuery = true)
    Set<Long> findFavoriteSeriesIds(@Param("userId") long userId);

    @Modifying
    @Query(value = """
                INSERT INTO user_favorite_movies(user_id, movie_id)
                VALUES (:userId, :movieId)
                ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int addMovie(@Param("userId") long userId, @Param("movieId") long movieId);

    @Modifying
    @Query(value = """
                DELETE FROM user_favorite_movies
                WHERE user_id = :userId AND movie_id = :movieId
            """, nativeQuery = true)
    int removeMovie(@Param("userId") long userId, @Param("movieId") long movieId);

    @Modifying
    @Query(value = """
                INSERT INTO user_favorite_series(user_id, tv_id)
                VALUES (:userId, :tvId)
                ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int addSeries(@Param("userId") long userId, @Param("tvId") long tvId);

    @Modifying
    @Query(value = """
                DELETE FROM user_favorite_series
                WHERE user_id = :userId AND tv_id = :tvId
            """, nativeQuery = true)
    int removeSeries(@Param("userId") long userId, @Param("tvId") long tvId);
}
