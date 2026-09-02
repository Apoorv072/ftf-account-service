package com.ftf.account_service.Service;

import com.ftf.account_service.AccountException.ResourceAlreadyExistsException;
import com.ftf.account_service.AccountException.ResourceNotFoundException;
import com.ftf.account_service.Dto.*;
import com.ftf.account_service.Entity.User;
import com.ftf.account_service.Entity.UserStatus;
import com.ftf.account_service.Mapper.MapperUtility;
import com.ftf.account_service.Repository.AccountRepository;
import com.ftf.account_service.Repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private final KafkaTemplate<String, GenerateNotificationEvent> kafkaNotificationTemplate;
    public UserServiceImpl(UserRepository userRepository, AccountRepository accountRepository,PasswordEncoder passwordEncoder, JwtService jwtService, RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper, KafkaTemplate<String, GenerateNotificationEvent> kafkaNotificationTemplate) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.kafkaNotificationTemplate = kafkaNotificationTemplate;
    }

    @Override
    public String  createUser(UserRequest request) {

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
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        PendingRegistration pending = new PendingRegistration();

        pending.setFirstName(request.getFirstName());
        pending.setLastName(request.getLastName());
        pending.setEmail(request.getEmail());
        pending.setPhoneNumber(request.getPhoneNumber());
        pending.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );
        pending.setOtp(String.valueOf(otp));

        String redisKey = "registration:otp:" + request.getEmail();

        try {
            String value = objectMapper.writeValueAsString(pending);

            redisTemplate.opsForValue().set(
                    redisKey,
                    value,
                    5,
                    TimeUnit.MINUTES
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to store registration details");
        }
        GenerateNotificationEvent generateNotificationEvent = new GenerateNotificationEvent();
        generateNotificationEvent.setGeneratedOTP(otp);
        generateNotificationEvent.setEmail(request.getEmail());
        kafkaNotificationTemplate.send("notifications",generateNotificationEvent);
        return "OTP sent successfully!";
    }

    @Override
    public UserResponse verifyOtp(VerifyOtpRequest request) {

        String redisKey = "registration:otp:" + request.getEmail();

        String value = redisTemplate.opsForValue().get(redisKey);

        if (value == null) {
            throw new RuntimeException("OTP expired or registration not found");
        }

        try {

            PendingRegistration pending =
                    objectMapper.readValue(value, PendingRegistration.class);

            if (!pending.getOtp().equals(request.getOtp())) {
                throw new RuntimeException("Invalid OTP");
            }

            User user = new User();

            user.setFirstName(pending.getFirstName());
            user.setLastName(pending.getLastName());
            user.setEmail(pending.getEmail());
            user.setPhoneNumber(pending.getPhoneNumber());
            user.setPasswordHash(pending.getPasswordHash());

            user.setStatus(UserStatus.ACTIVE);

            LocalDateTime now = LocalDateTime.now();
            user.setCreatedAt(now);
            user.setUpdatedAt(now);

            User savedUser = userRepository.save(user);

            // Remove pending registration after successful verification
            redisTemplate.delete(redisKey);

            return MapperUtility.mapToUserResponse(savedUser);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process registration data");
        }
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