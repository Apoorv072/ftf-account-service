package com.ftf.account_service.Repository;

import com.ftf.account_service.Entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);
    List<Account> findByUserId(Long userId);

    @Modifying
    @Query("""
    UPDATE Account a
    SET a.balance = a.balance - :amount,
        a.updatedAt = CURRENT_TIMESTAMP
    WHERE a.id = :accountId
      AND a.status = 'ACTIVE'
      AND a.currency = :currency
      AND a.balance >= :amount
""")
    int debitAccount(
            @Param("accountId") Long accountId,
            @Param("amount") BigDecimal amount,
            @Param("currency") String currency
    );

    @Modifying
    @Query("""
    UPDATE Account a
    SET a.balance = a.balance + :amount,
        a.updatedAt = CURRENT_TIMESTAMP
    WHERE a.id = :accountId
      AND a.status = 'ACTIVE'
      AND a.currency = :currency
""")
    int creditAccount(
            @Param("accountId") Long accountId,
            @Param("amount") BigDecimal amount,
            @Param("currency") String currency
    );
}