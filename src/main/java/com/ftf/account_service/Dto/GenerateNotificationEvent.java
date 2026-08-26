package com.ftf.account_service.Dto;

import lombok.Data;

@Data
public class GenerateNotificationEvent {
    private String email;
    private int generatedOTP;
}
