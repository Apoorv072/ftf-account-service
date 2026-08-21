package com.ftf.account_service.Mapper;

import com.ftf.account_service.Dto.AccountCreatedEvent;
import com.ftf.account_service.Dto.AccountResponse;
import com.ftf.account_service.Dto.UserResponse;
import com.ftf.account_service.Entity.Account;
import com.ftf.account_service.Entity.User;

import java.math.BigDecimal;

public class MapperUtility {
    private MapperUtility() {}
    public static AccountResponse mapToAccountResponse(Account account) {
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
    public static UserResponse mapToUserResponse(User user) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }

    public static AccountCreatedEvent mapToAccountCreatedEvent(Account account){
        AccountCreatedEvent event = new AccountCreatedEvent();

        event.setAccountId(account.getId());
        event.setStatus(account.getStatus());
        event.setCurrency(account.getCurrency());
        event.setDailyAmountLimit(BigDecimal.valueOf(100000));
        event.setPerTransactionLimit(BigDecimal.valueOf(50000));
        event.setDailyTransactionCountLimit(20);

        return event;
    }
}
