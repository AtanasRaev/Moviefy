package com.moviefy.service.scheduling.cleanup;

import com.moviefy.database.model.entity.user.AppUser;
import com.moviefy.database.repository.user.EmailVerificationTokenRepository;
import com.moviefy.database.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountCleanupService {
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final static Logger logger = LoggerFactory.getLogger(AccountCleanupService.class);

    public AccountCleanupService(UserRepository userRepository, 
                                EmailVerificationTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(cron = "0 0 1 ? * SUN")
    @Transactional
    public void cleanupAccountsAndTokens() {
        LocalDateTime now = LocalDateTime.now();
        
        int deletedTokens = tokenRepository.deleteExpiredTokens(now);
        logger.info("Deleted {} expired email verification tokens", deletedTokens);
        
        LocalDateTime userThreshold = now.minusHours(24);
        List<AppUser> usersToDelete = userRepository.findUnverifiedUsersWithoutValidTokens(userThreshold, now);
        
        if (!usersToDelete.isEmpty()) {
            for (AppUser u : usersToDelete) {
                u.getFavoriteMovies().clear();
                u.getFavoriteSeries().clear();
                u.getRoles().clear();
            }
            
            userRepository.deleteAll(usersToDelete);
            logger.info("Deleted {} users without valid verification tokens", usersToDelete.size());
        }
    }
}