package com.ftf.account_service.Service;

import com.ftf.account_service.Dto.AccountRequest;
import com.ftf.account_service.Dto.AccountResponse;
import com.ftf.account_service.Dto.InternalTransferRequest;
import com.ftf.account_service.Dto.LoginRequest;

public interface AccountService {
    AccountResponse createAccount(AccountRequest request);
    AccountResponse getAccountById(Long id);
    AccountResponse getAccountByNumber(String accountNumber);
    public void transfer(InternalTransferRequest request);
}