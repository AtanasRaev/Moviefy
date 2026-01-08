package com.moviefy.web;

import com.moviefy.database.model.dto.pageDto.user.UserProfileDTO;
import com.moviefy.database.model.dto.response.ApiResponse;
import com.moviefy.service.auth.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDTO>> me(Authentication auth) {
        String email = requireEmail(auth);

        UserProfileDTO user = this.userService.getProfile(email);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "User profile retrieved successfully",
                user
        ));
    }

    @GetMapping("/favorites/movies/ids")
    public ResponseEntity<ApiResponse<Set<Long>>> movieIds(Authentication auth) {
        String email = requireEmail(auth);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Movie ids retrieved successfully",
                this.userService.getFavoriteMovieIds(email)
        ));
    }

    @GetMapping("/favorites/series/ids")
    public ResponseEntity<ApiResponse<Set<Long>>> seriesIds(Authentication auth) {
        String email = requireEmail(auth);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Series ids retrieved successfully",
                this.userService.getFavoriteSeriesIds(email)
        ));
    }

    @PostMapping("/favorites/movies/{movieId}")
    public ResponseEntity<ApiResponse<Void>> addMovie(@PathVariable long movieId, Authentication auth) {
        String email = requireEmail(auth);
        this.userService.addMovie(email, movieId);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Successfully added movie to favorites.",
                null
        ));
    }

    @DeleteMapping("/favorites/movies/{movieId}")
    public ResponseEntity<ApiResponse<Void>> removeMovie(@PathVariable long movieId, Authentication auth) {
        String email = requireEmail(auth);
        this.userService.removeMovie(email, movieId);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Successfully removed movie from favorites.",
                null
        ));
    }

    @PostMapping("/favorites/series/{tvId}")
    public ResponseEntity<ApiResponse<Void>> addSeries(@PathVariable long tvId, Authentication auth) {
        String email = requireEmail(auth);
        this.userService.addSeries(email, tvId);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Successfully added series to favorites.",
                null
        ));
    }

    @DeleteMapping("/favorites/series/{tvId}")
    public ResponseEntity<ApiResponse<Void>> removeSeries(@PathVariable long tvId, Authentication auth) {
        String email = requireEmail(auth);
        this.userService.removeSeries(email, tvId);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Successfully removed series from favorites.",
                null
        ));
    }

    private String requireEmail(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return auth.getName();
    }
}
