package com.moviefy.service.auth;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class SessionService {

    private final Optional<FindByIndexNameSessionRepository<? extends Session>> sessions;

    public SessionService(Optional<FindByIndexNameSessionRepository<? extends Session>> sessions) {
        this.sessions = sessions;
    }

    public void logoutEverywhere(String principalName) {
        if (sessions.isEmpty()) {
            return;
        }

        FindByIndexNameSessionRepository<? extends Session> sessionRepository = sessions.get();
        Map<String, ? extends Session> userSessions =
                sessionRepository.findByIndexNameAndIndexValue(
                        FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                        principalName
                );

        for (String sessionId : userSessions.keySet()) {
            sessionRepository.deleteById(sessionId);
        }
    }
}
