package com.moviefy.web;

import com.moviefy.database.model.dto.databaseDto.user.EmailVerificationTokenDTO;
import com.moviefy.database.model.dto.databaseDto.user.PasswordResetConfirmDTO;
import com.moviefy.database.model.dto.databaseDto.user.PasswordResetRequestDTO;
import com.moviefy.database.model.dto.databaseDto.user.RegisterUserDTO;
import com.moviefy.database.model.dto.response.ApiResponse;
import com.moviefy.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterUserDTO registerUserDTO) {
        this.authService.register(registerUserDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "Registration successful. Please check your email to verify your account.",
                        null
                ));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody EmailVerificationTokenDTO emailVerificationTokenDTO) {
        this.authService.verifyEmail(emailVerificationTokenDTO);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Email verified successfully.",
                        null
                ));
    }

    @PostMapping("/resend-email")
    public ResponseEntity<ApiResponse<Void>> resendEmail(@Valid @RequestBody EmailVerificationTokenDTO emailVerificationTokenDTO) {
        this.authService.resendEmailVerification(emailVerificationTokenDTO);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Email verification link resent successfully.",
                        null
                ));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody PasswordResetRequestDTO passwordResetRequestDTO) {
        this.authService.requestPasswordReset(passwordResetRequestDTO);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "If an account with that email exists, a password reset email has been sent.",
                        null
                ));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmDTO passwordResetConfirmDTO) {
        this.authService.confirmPasswordReset(passwordResetConfirmDTO);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Password reset successful.",
                        null
                ));
    }
}
