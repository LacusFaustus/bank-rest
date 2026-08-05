package com.bank.dto;

import com.bank.validation.StrongPassword;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @StrongPassword(message = "Password does not meet security requirements")
    private String password;

    @JsonCreator
    public AuthRequest(
            @JsonProperty("username") String username,
            @JsonProperty("password") String password) {
        this.username = username;
        this.password = password;
    }

    public AuthRequest() {
        // Default constructor for Jackson
    }
}
