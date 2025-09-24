package com.nextinnomind.campusnestbackend.dto.chat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationResponse {
    private String conversationId;
    private UserInfo otherUser;
    private MessageResponse lastMessage;
    private Integer unreadCount;

    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String fullName;
        private String profilePictureUrl;
        private String role;
    }
}
