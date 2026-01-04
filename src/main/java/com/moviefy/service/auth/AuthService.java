package com.moviefy.service.auth;

import com.moviefy.database.model.dto.databaseDto.user.EmailVerificationTokenDTO;
import com.moviefy.database.model.dto.databaseDto.user.PasswordResetConfirmDTO;
import com.moviefy.database.model.dto.databaseDto.user.PasswordResetRequestDTO;
import com.moviefy.database.model.dto.databaseDto.user.RegisterUserDTO;

public interface AuthService {
    void register(RegisterUserDTO registerUserDTO);

    void verifyEmail(EmailVerificationTokenDTO emailVerificationTokenDTO);

    void resendEmailVerification(EmailVerificationTokenDTO emailVerificationTokenDTO);

    void requestPasswordReset(PasswordResetRequestDTO passwordResetRequestDTO);

    void confirmPasswordReset(PasswordResetConfirmDTO passwordResetConfirmDTO);
}
