package com.moviefy.database.model.dto.databaseDto.user;

import jakarta.validation.constraints.NotBlank;

public class EmailVerificationTokenDTO {
    @NotBlank
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
