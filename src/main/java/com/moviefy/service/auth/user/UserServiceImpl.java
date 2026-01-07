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
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        UserDTO dto = modelMapper.map(user, UserDTO.class);
        dto.setRoles(user.getRoles().stream().map(Enum::name).toList());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(String email) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

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
}
