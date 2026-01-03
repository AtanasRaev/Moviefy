package com.moviefy.service.scheduling.cleanup;

import com.moviefy.database.model.entity.user.AppUser;
import com.moviefy.database.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserCleanupService {
    private final UserRepository userRepository;
    private final static Logger logger = LoggerFactory.getLogger(UserCleanupService.class);

    public UserCleanupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 1 ? * SUN")
    @Transactional
    public void cleanupOldUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);

        List<AppUser> users = this.userRepository.findAllNotVerifiedUsersOlderThan(threshold);
        if (users.isEmpty()) {
            return;
        }

        for (AppUser u : users) {
            u.getFavoriteMovies().clear();
            u.getFavoriteSeries().clear();
            u.getRoles().clear();
        }

        this.userRepository.deleteAll(users);
        logger.info("Deleted {} users", users.size());
    }
}
