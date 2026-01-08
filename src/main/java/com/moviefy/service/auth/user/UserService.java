package com.moviefy.service.auth.user;

import com.moviefy.database.model.dto.databaseDto.UserDTO;
import com.moviefy.database.model.dto.pageDto.user.UserProfileDTO;

import java.util.Set;

public interface UserService {
    UserDTO getByEmail(String email);

    UserProfileDTO getProfile(String email);

    Set<Long> getFavoriteMovieIds(String email);

    Set<Long> getFavoriteSeriesIds(String email);

    void addMovie(String email, long movieId);

    void removeMovie(String email, long movieId);

    void addSeries(String email, long tvId);

    void removeSeries(String email, long tvId);
}
