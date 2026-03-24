package com.moviefy.service.auth;

import org.springframework.stereotype.Service;

@Service
public class SessionService {

    public void logoutEverywhere(String principalName) {
        // Redis-backed global session invalidation is temporarily disabled.
    }
}
