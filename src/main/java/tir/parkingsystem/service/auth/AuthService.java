package tir.parkingsystem.service.auth;

import tir.parkingsystem.entity.dto.request.LoginRequest;
import tir.parkingsystem.entity.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse authenticate(LoginRequest loginRequest);
}
