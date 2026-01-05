package com.moviefy.service.auth.user;

import com.moviefy.database.model.dto.databaseDto.UserDTO;

public interface UserService {
    UserDTO getByEmail(String email);
}
