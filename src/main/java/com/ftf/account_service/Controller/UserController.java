package com.ftf.account_service.Controller;

import com.ftf.account_service.Dto.*;
import com.ftf.account_service.Service.UserService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {

        UserResponse response = userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id)
    {
        UserResponse response = userService.getById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/{id}/accounts")
    public ResponseEntity<List<AccountResponse>> getAccount(@PathVariable Long id)
    {
        List<AccountResponse> accounts =  userService.getUserAccounts(id);

        return ResponseEntity.status(HttpStatus.OK).body(accounts);
    }
    @PostMapping("/login")
    public ResponseEntity<?> userLogin(@RequestBody LoginRequest request)
    {
        LoginResponse response = userService.userLogin(request);
        if (response==null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid usernsme or password");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
