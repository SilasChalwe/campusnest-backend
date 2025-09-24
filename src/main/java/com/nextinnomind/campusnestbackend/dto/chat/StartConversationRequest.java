package com.nextinnomind.campusnestbackend.dto.chat;

import lombok.Data;

@Data
public class StartConversationRequest {
    private Long receiverId;       // ID of the user you want to start a conversation with
    private String initialMessage; // Optional initial message to send
}
