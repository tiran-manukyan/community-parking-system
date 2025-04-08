package tir.parkingsystem.service.user;

import tir.parkingsystem.entity.domain.UserEntity;
import tir.parkingsystem.entity.dto.request.RegisterRequest;

public interface UserService {
    UserEntity registerUser(RegisterRequest registerRequest);
}
