package com.ftf.account_service.Repository;

import com.ftf.account_service.Entity.TransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransferRequestRepository extends JpaRepository<TransferRequest, Long>
{
    Optional<TransferRequest> findByTransactionReference(String transactionReference);
}
