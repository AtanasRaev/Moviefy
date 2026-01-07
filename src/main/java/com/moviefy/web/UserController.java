package com.moviefy.web;

import com.moviefy.database.model.dto.pageDto.user.UserProfileDTO;
import com.moviefy.database.model.dto.response.ApiResponse;
import com.moviefy.service.auth.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getUsers(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "Unauthorized"));
        }

        UserProfileDTO user = this.userService.getProfile(auth.getName());
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        "User profile retrieved successfully",
                        user
                ));
    }
}
