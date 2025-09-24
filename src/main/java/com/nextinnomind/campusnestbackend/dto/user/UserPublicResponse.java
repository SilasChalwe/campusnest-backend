package com.nextinnomind.campusnestbackend.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPublicResponse {
    private Long id;
    private String fullName;
    private String role;
    private String profilePictureUrl;
}
