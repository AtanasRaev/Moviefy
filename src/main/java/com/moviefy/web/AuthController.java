package com.moviefy.web;

import com.moviefy.database.model.dto.databaseDto.UserDTO;
import com.moviefy.database.model.dto.databaseDto.user.*;
import com.moviefy.database.model.dto.response.ApiResponse;
import com.moviefy.service.auth.AuthService;
import com.moviefy.service.auth.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService,
                          UserService userService) {
        this.authService = authService;
        this.userService = userService;
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
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Email verified successfully.",
                        null
                ));
    }

    @PostMapping("/resend-email")
    public ResponseEntity<ApiResponse<Void>> resendEmail(@Valid @RequestBody EmailVerificationTokenDTO emailVerificationTokenDTO) {
        this.authService.resendEmailVerification(emailVerificationTokenDTO);
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Email verification link resent successfully.",
                        null
                ));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody PasswordResetRequestDTO passwordResetRequestDTO) {
        this.authService.requestPasswordReset(passwordResetRequestDTO);

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "If an account with that email exists, a password reset email has been sent.",
                        null
                ));
    }

    @PostMapping("/password-reset/token-check")
    public ResponseEntity<ApiResponse<Void>> checkPasswordResetToken(@Valid @RequestBody PasswordResetTokenCheckDTO passwordResetTokenCheckDTO) {
        this.authService.checkPasswordResetToken(passwordResetTokenCheckDTO);

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Token is valid.",
                        null
                ));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmDTO passwordResetConfirmDTO) {
        this.authService.confirmPasswordReset(passwordResetConfirmDTO);

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Password reset successful.",
                        null
                ));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Unauthorized"));
        }

        UserDTO user = this.userService.getByEmail(auth.getName());

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "User is authenticated",
                        user
                ));
    }
}
