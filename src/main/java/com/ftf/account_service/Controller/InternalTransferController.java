package com.ftf.account_service.Controller;

import com.ftf.account_service.Dto.InternalTransferRequest;
import com.ftf.account_service.Service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class InternalTransferController {

    private final AccountService accountService;

    public InternalTransferController(AccountService accountService)
    {
        this.accountService=accountService;
    }
    @PostMapping("/internal/transfers")
    public ResponseEntity<Void> transfer(@Valid @RequestBody InternalTransferRequest request) {

        accountService.transfer(request);

        return ResponseEntity.ok().build();
    }
}
