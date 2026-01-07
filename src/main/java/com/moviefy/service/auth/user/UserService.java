package com.moviefy.service.auth.user;

import com.moviefy.database.model.dto.databaseDto.UserDTO;
import com.moviefy.database.model.dto.pageDto.user.UserProfileDTO;
import org.springframework.security.core.Authentication;

public interface UserService {
    UserDTO getByEmail(String email);

    UserProfileDTO getProfile(String email);
}
