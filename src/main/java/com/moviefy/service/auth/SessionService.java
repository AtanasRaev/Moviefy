package com.moviefy.service.auth;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SessionService {

    private final FindByIndexNameSessionRepository<? extends Session> sessions;

    public SessionService(FindByIndexNameSessionRepository<? extends Session> sessions) {
        this.sessions = sessions;
    }

    public void logoutEverywhere(String principalName) {
        Map<String, ? extends Session> userSessions =
                sessions.findByIndexNameAndIndexValue(
                        FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                        principalName
                );

        for (String sessionId : userSessions.keySet()) {
            sessions.deleteById(sessionId);
        }
    }
}
