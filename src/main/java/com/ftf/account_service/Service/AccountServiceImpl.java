package com.ftf.account_service.Service;

import com.ftf.account_service.AccountException.ResourceNotFoundException;
import com.ftf.account_service.Dto.AccountRequest;
import com.ftf.account_service.Dto.AccountResponse;
import com.ftf.account_service.Entity.Account;
import com.ftf.account_service.Entity.AccountStatus;
import com.ftf.account_service.Entity.User;
import com.ftf.account_service.Repository.AccountRepository;
import com.ftf.account_service.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountServiceImpl(
            AccountRepository accountRepository,
            UserRepository userRepository) {

        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AccountResponse createAccount(AccountRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + request.getUserId()
                        )
                );

        Account account = new Account();

        account.setAccountNumber(generateAccountNumber());
        account.setUser(user);
        account.setAccountType(request.getAccountType());
        account.setCurrency(request.getCurrency().toUpperCase());
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);

        LocalDateTime now = LocalDateTime.now();

        account.setCreatedAt(now);
        account.setUpdatedAt(now);

        Account savedAccount = accountRepository.save(account);

        return mapToResponse(savedAccount);
    }

    private String generateAccountNumber() {

        return "FTF-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 20)
                        .toUpperCase();
    }

    private AccountResponse mapToResponse(Account account) {

        AccountResponse response = new AccountResponse();

        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setUserId(account.getUser().getId());
        response.setAccountType(account.getAccountType());
        response.setCurrency(account.getCurrency());
        response.setBalance(account.getBalance());
        response.setStatus(account.getStatus());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());

        return response;
    }
}