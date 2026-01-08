package com.moviefy.service.auth.user;

import com.moviefy.database.model.dto.databaseDto.UserDTO;
import com.moviefy.database.model.dto.pageDto.mediaDto.movieDto.MoviePageWithGenreProjection;
import com.moviefy.database.model.dto.pageDto.mediaDto.tvSeriesDto.TvSeriesPageWithGenreProjection;
import com.moviefy.database.model.dto.pageDto.user.UserProfileDTO;
import com.moviefy.database.model.entity.user.AppUser;
import com.moviefy.database.repository.media.MovieRepository;
import com.moviefy.database.repository.media.tvSeries.TvSeriesRepository;
import com.moviefy.database.repository.user.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final TvSeriesRepository tvSeriesRepository;
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository,
                           MovieRepository movieRepository,
                           TvSeriesRepository tvSeriesRepository,
                           ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.tvSeriesRepository = tvSeriesRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getByEmail(String email) {
        AppUser user = findUserByEmail(email);

        UserDTO dto = modelMapper.map(user, UserDTO.class);
        dto.setRoles(user.getRoles().stream().map(Enum::name).toList());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(String email) {
        AppUser user = findUserByEmail(email);

        List<MoviePageWithGenreProjection> movies = this.movieRepository.findFavoriteMovies(user.getId());
        List<TvSeriesPageWithGenreProjection> series = this.tvSeriesRepository.findFavoriteTvSeries(user.getId());

        UserProfileDTO dto = new UserProfileDTO();
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setFavoriteMovies(movies);
        dto.setFavoriteTvSeries(series);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getFavoriteMovieIds(String email) {
        long userId = requireUserId(email);
        return this.userRepository.findFavoriteMovieIds(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getFavoriteSeriesIds(String email) {
        long userId = requireUserId(email);
        return this.userRepository.findFavoriteSeriesIds(userId);
    }

    @Override
    @Transactional
    public void addMovie(String email, long movieId) {
        long userId = requireUserId(email);
        this.userRepository.addMovie(userId, movieId);
    }

    @Override
    @Transactional
    public void removeMovie(String email, long movieId) {
        long userId = requireUserId(email);
        this.userRepository.removeMovie(userId, movieId);
    }

    @Override
    @Transactional
    public void addSeries(String email, long tvId) {
        long userId = requireUserId(email);
        this.userRepository.addSeries(userId, tvId);
    }

    @Override
    @Transactional
    public void removeSeries(String email, long tvId) {
        long userId = requireUserId(email);
        this.userRepository.removeSeries(userId, tvId);
    }

    private AppUser findUserByEmail(String email) {
        return this.userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
    }

    private long requireUserId(String email) {
        Long id = userRepository.findIdByEmail(email);
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return id;
    }
}
