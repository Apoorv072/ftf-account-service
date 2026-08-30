package com.ftf.account_service.Dto;

import lombok.Data;

@Data
public class LoginResponse {
    String tokenType;
    String token;
}
