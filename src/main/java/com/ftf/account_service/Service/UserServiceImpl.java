package com.ftf.account_service.Service;

import com.ftf.account_service.AccountException.ResourceAlreadyExistsException;
import com.ftf.account_service.AccountException.ResourceNotFoundException;
import com.ftf.account_service.Dto.*;
import com.ftf.account_service.Entity.User;
import com.ftf.account_service.Entity.UserStatus;
import com.ftf.account_service.Mapper.MapperUtility;
import com.ftf.account_service.Repository.AccountRepository;
import com.ftf.account_service.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public UserServiceImpl(UserRepository userRepository, AccountRepository accountRepository,PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public UserResponse createUser(UserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException(
                    "User with email " + request.getEmail() + " already exists"
            );
        }

        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResourceAlreadyExistsException(
                    "User with Phone number " + request.getEmail() + " already exists"
            );
        }

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordHash( passwordEncoder.encode(request.getPassword()));    //  Generates the hash code of the password
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);

        return MapperUtility.mapToUserResponse(savedUser);
    }

    @Override
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return MapperUtility.mapToUserResponse(user);
    }

    @Override
    public List<AccountResponse> getUserAccounts(Long userId) {

        userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return accountRepository.findByUserId(userId).stream().map(account -> MapperUtility.mapToAccountResponse(account)).toList();
    }

    @Override
    public LoginResponse userLogin(LoginRequest request) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        LoginResponse response = new LoginResponse();
        if (user.isPresent()) {
            if (passwordEncoder.matches(request.getPassword(), user.get().getPasswordHash())) {
                response.setToken(jwtService.generateJwtToken(user.get()));
                response.setTokenType("Bearer");
                return response;
            }
        }
        return null;
    }
}