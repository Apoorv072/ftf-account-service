package com.ftf.account_service.Service;

import com.ftf.account_service.AccountException.ResourceNotFoundException;
import com.ftf.account_service.Dto.*;
import com.ftf.account_service.Entity.*;
import com.ftf.account_service.Mapper.MapperUtility;
import com.ftf.account_service.Repository.AccountRepository;
import com.ftf.account_service.Repository.TransferRequestRepository;
import com.ftf.account_service.Repository.UserRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, AccountCreatedEvent> kafkaTemplate;

    private final KafkaTemplate<String, GenerateNotificationEvent> kafkaNotificationTemplate;
    private final TransferRequestRepository transferRequestRepository;

    public AccountServiceImpl(AccountRepository accountRepository,
                              UserRepository userRepository,
                              TransferRequestRepository transferRequestRepository,
                              KafkaTemplate<String, AccountCreatedEvent> kafkaTemplate,
                              KafkaTemplate<String, GenerateNotificationEvent> kafkaNotificationTemplate)
    {
        this.transferRequestRepository = transferRequestRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaNotificationTemplate = kafkaNotificationTemplate;
    }
    @Override
    public AccountResponse getAccountById(Long id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        return MapperUtility.mapToAccountResponse(account);
    }

    @Override
    public AccountResponse getAccountByNumber(String accountNumber) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with account number: " + accountNumber));

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

        AccountCreatedEvent accountCreatedEvent = MapperUtility.mapToAccountCreatedEvent(savedAccount);
        kafkaTemplate.send("account-created",accountCreatedEvent);
        GenerateNotificationEvent generateNotificationEvent = new GenerateNotificationEvent();
        generateNotificationEvent.setEmail(user.getEmail());
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        generateNotificationEvent.setGeneratedOTP(otp);
        kafkaNotificationTemplate.send("notifications",generateNotificationEvent);

    return MapperUtility.mapToAccountResponse(savedAccount);
    }

    @Transactional
    public void transfer(InternalTransferRequest request) {

        Optional<TransferRequest> existingRequest = transferRequestRepository.findByTransactionReference(request.getTransactionReference());
        TransferRequest newTransferRequest = new TransferRequest();

        if (existingRequest.isPresent()) {

            if ("COMPLETED".equals(existingRequest.get().getStatus())) {
                return;
            }

            throw new IllegalStateException("Transfer already exists with status: " + existingRequest.get().getStatus());
        }

    try {
        int debited = accountRepository.debitAccount(request.getSourceAccountId(), request.getAmount(), request.getCurrency());

        if (debited != 1) {
            newTransferRequest.setStatus(TransactionStatus.FAILED);
            throw new IllegalStateException("Unable to debit source account");
        }

        int credited = accountRepository.creditAccount(request.getDestinationAccountId(), request.getAmount(), request.getCurrency());

        if (credited != 1) {
            newTransferRequest.setStatus(TransactionStatus.FAILED);
            throw new IllegalStateException("Unable to credit destination account");
        }

        newTransferRequest.setStatus(TransactionStatus.COMPLETED);

    }finally {
        newTransferRequest.setAmount(request.getAmount());
        newTransferRequest.setCurrency(request.getCurrency());
        newTransferRequest.setSourceAccountId(request.getSourceAccountId());
        newTransferRequest.setDestinationAccountId(request.getDestinationAccountId());
        newTransferRequest.setTransactionReference(request.getTransactionReference());
        newTransferRequest.setCreatedAt(LocalDateTime.now());
        transferRequestRepository.save(newTransferRequest);
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