package com.nextinnomind.campusnestbackend.dto.chat;

import lombok.Data;

@Data
public class SendMessageRequest {
    private String conversationId;
    private String content;
}
