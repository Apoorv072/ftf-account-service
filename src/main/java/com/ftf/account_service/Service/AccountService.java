package com.ftf.account_service.Service;

import com.ftf.account_service.Dto.AccountRequest;
import com.ftf.account_service.Dto.AccountResponse;

public interface AccountService {
    AccountResponse createAccount(AccountRequest request);
    AccountResponse getAccountById(Long id);
    AccountResponse getAccountByNumber(String accountNumber);
}