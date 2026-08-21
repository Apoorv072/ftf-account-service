package com.ftf.account_service.Dto;

import com.ftf.account_service.Entity.AccountStatus;
import jakarta.persistence.Column;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountCreatedEvent {
    private Long accountId;

    private String currency;

    private BigDecimal dailyAmountLimit;

    private BigDecimal perTransactionLimit;

    private Integer dailyTransactionCountLimit;

    private AccountStatus status;
}
