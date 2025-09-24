package com.nextinnomind.campusnestbackend.dto.user;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phone;
    private String address;
}
