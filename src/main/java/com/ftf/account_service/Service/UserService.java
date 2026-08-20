package com.ftf.account_service.Service;

import com.ftf.account_service.Dto.AccountResponse;
import com.ftf.account_service.Dto.UserRequest;
import com.ftf.account_service.Dto.UserResponse;
import com.ftf.account_service.Entity.Account;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse getById(Long id);
    List<AccountResponse> getUserAccounts(Long userId);
}
