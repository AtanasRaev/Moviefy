package com.moviefy.service.auth;

import com.moviefy.database.model.dto.databaseDto.user.EmailVerificationTokenDTO;
import com.moviefy.database.model.dto.databaseDto.user.RegisterUserDTO;

public interface AuthService {
    void register(RegisterUserDTO registerUserDTO);

    void verifyEmail(EmailVerificationTokenDTO emailVerificationTokenDTO);
}
