package com.moviefy.database.model.dto.databaseDto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterUserDTO {
    @Email
    @NotBlank
    @Size(max = 120)
    private String email;

    @Size(max = 20)
    @JsonProperty("first_name")
    private String firstName;

    @Size(max = 20)
    @JsonProperty("last_name")
    private String lastName;

    @NotBlank
    @Size(min = 5, max = 72)
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
