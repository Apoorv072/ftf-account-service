package com.ftf.account_service.Dto;

import lombok.Data;

@Data
public class PendingRegistration {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private String otp;
}
