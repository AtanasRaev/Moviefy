package com.moviefy.service.auth;

import com.moviefy.database.model.dto.databaseDto.user.*;

public interface AuthService {
    void register(RegisterUserDTO registerUserDTO);

    void verifyEmail(EmailVerificationTokenDTO emailVerificationTokenDTO);

    void resendEmailVerification(EmailVerificationTokenDTO emailVerificationTokenDTO);

    void requestPasswordReset(PasswordResetRequestDTO passwordResetRequestDTO);

    void confirmPasswordReset(PasswordResetConfirmDTO passwordResetConfirmDTO);

    void checkPasswordResetToken(PasswordResetTokenCheckDTO passwordResetTokenCheckDTO);
}
