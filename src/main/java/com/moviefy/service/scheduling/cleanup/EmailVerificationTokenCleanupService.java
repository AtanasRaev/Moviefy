package com.moviefy.service.scheduling.cleanup;

import com.moviefy.database.repository.EmailVerificationTokenRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailVerificationTokenCleanupService {
    private final EmailVerificationTokenRepository tokenRepository;
    private final static Logger logger = LoggerFactory.getLogger(EmailVerificationTokenCleanupService.class);

    public EmailVerificationTokenCleanupService(EmailVerificationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(cron = "0 0 1 */3 * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        logger.info("Deleted {} expired email verification tokens", deleted);
    }
}
