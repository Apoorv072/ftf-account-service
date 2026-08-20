package com.ftf.account_service.Service;

import com.ftf.account_service.AccountException.ResourceNotFoundException;
import com.ftf.account_service.Dto.AccountRequest;
import com.ftf.account_service.Dto.AccountResponse;
import com.ftf.account_service.Dto.InternalTransferRequest;
import com.ftf.account_service.Entity.Account;
import com.ftf.account_service.Entity.AccountStatus;
import com.ftf.account_service.Entity.User;
import com.ftf.account_service.Mapper.MapperUtility;
import com.ftf.account_service.Repository.AccountRepository;
import com.ftf.account_service.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public AccountResponse getAccountById(Long id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with id: " + id
                        )
                );

        return MapperUtility.mapToAccountResponse(account);
    }

    @Override
    public AccountResponse getAccountByNumber(String accountNumber) {

        Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with account number: "
                                        + accountNumber
                        )
                );

        return MapperUtility.mapToAccountResponse(account);
    }
    @Override
    public AccountResponse createAccount(AccountRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Account account = new Account();

        account.setAccountNumber(generateAccountNumber());
        account.setUser(user);
        account.setAccountType(request.getAccountType());
        account.setCurrency(request.getCurrency().toUpperCase());
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        Account savedAccount = accountRepository.save(account);

        return MapperUtility.mapToAccountResponse(savedAccount);
    }

    @Transactional
    public void transfer(InternalTransferRequest request) {

        if (request.getSourceAccountId()
                .equals(request.getDestinationAccountId())) {

            throw new IllegalArgumentException(
                    "Source and destination accounts cannot be the same"
            );
        }

        int debited = accountRepository.debitAccount(
                request.getSourceAccountId(),
                request.getAmount(),
                request.getCurrency()
        );

        if (debited != 1) {
            throw new IllegalStateException(
                    "Unable to debit source account"
            );
        }

        int credited = accountRepository.creditAccount(
                request.getDestinationAccountId(),
                request.getAmount(),
                request.getCurrency()
        );

        if (credited != 1) {
            throw new IllegalStateException(
                    "Unable to credit destination account"
            );
        }
    }

    private String generateAccountNumber() {

        return "FTF-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 20)
                        .toUpperCase();
    }

}