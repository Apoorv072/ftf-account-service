package com.ftf.account_service.Service;

import com.ftf.account_service.Dto.*;
import com.ftf.account_service.Entity.Account;
import com.ftf.account_service.Entity.User;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse getById(Long id);
    List<AccountResponse> getUserAccounts(Long userId);
    LoginResponse userLogin(LoginRequest request);
}
