package com.ftf.account_service.Service;

import com.ftf.account_service.Dto.UserRequest;
import com.ftf.account_service.Dto.UserResponse;
public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse getById(int id);
}
