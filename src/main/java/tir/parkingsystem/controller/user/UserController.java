package tir.parkingsystem.controller.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tir.parkingsystem.entity.dto.request.LoginRequest;
import tir.parkingsystem.entity.dto.request.RegisterRequest;
import tir.parkingsystem.entity.dto.response.AuthResponse;
import tir.parkingsystem.service.auth.AuthService;
import tir.parkingsystem.service.user.UserService;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @NotNull @Valid LoginRequest loginRequest) {
        AuthResponse authResponse = authService.authenticate(loginRequest);

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    public Long register(@RequestBody @NotNull @Valid RegisterRequest registerRequest) {
        return userService.registerUser(registerRequest).getId();
    }
}
