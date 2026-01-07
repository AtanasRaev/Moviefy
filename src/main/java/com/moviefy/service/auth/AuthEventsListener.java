package com.moviefy.service.auth;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AuthEventsListener {

    private final SessionService sessionService;

    public AuthEventsListener(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordResetConfirmed(PasswordResetConfirmedEvent event) {
        sessionService.logoutEverywhere(event.principal());
    }
}
