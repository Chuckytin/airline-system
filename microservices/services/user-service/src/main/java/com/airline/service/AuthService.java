package com.airline.service;

import com.airline.payload.request.LoginRequest;
import com.airline.payload.request.UserRequest;
import com.airline.payload.response.AuthResponse;

public interface AuthService {

    AuthResponse signUp(UserRequest userRequest);

    AuthResponse login(LoginRequest request);

    void logout(String token);

}
