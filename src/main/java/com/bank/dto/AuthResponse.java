package com.bank.dto;

import java.util.Objects;

public class AuthResponse {
    private String token;
    private String type;

    public AuthResponse() {}

    public AuthResponse(String token, String type) {
        this.token = token;
        this.type = type;
    }

    public AuthResponse(String token) {
        this(token, "Bearer");
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthResponse that = (AuthResponse) o;
        return Objects.equals(token, that.token) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, type);
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "token='" + token + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
