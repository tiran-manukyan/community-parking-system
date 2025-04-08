package tir.parkingsystem.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tir.parkingsystem.entity.domain.UserEntity;
import tir.parkingsystem.entity.dto.request.RegisterRequest;
import tir.parkingsystem.exception.ParkingIllegalStateException;
import tir.parkingsystem.repository.user.UserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserEntity registerUser(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new ParkingIllegalStateException("Email already in use");
        }

        UserEntity user = UserEntity.builder()
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .build();

        return userRepository.save(user);
    }
}
