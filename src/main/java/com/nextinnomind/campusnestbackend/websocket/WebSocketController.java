package com.nextinnomind.campusnestbackend.websocket;

import com.nextinnomind.campusnestbackend.dto.chat.MessageResponse;
import com.nextinnomind.campusnestbackend.dto.chat.SendMessageRequest;
import com.nextinnomind.campusnestbackend.security.UserPrincipal;
import com.nextinnomind.campusnestbackend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.send")
    @SendToUser("/queue/messages")
    public MessageResponse sendMessage(
            @Payload SendMessageRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Header("conversationId") Long conversationId) {

        MessageResponse message = chatService.sendMessage(String.valueOf(conversationId), request, userPrincipal.getId());

        // Send to recipient
        messagingTemplate.convertAndSendToUser(
                message.getId().toString(),
                "/queue/messages",
                message
        );

        return message;
    }

    @MessageMapping("/chat.typing")
    public void typing(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Header("conversationId") Long conversationId,
            @Header("recipientId") Long recipientId) {

        messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/typing",
                Map.of("conversationId", conversationId, "userId", userPrincipal.getId())
        );
    }

    @MessageMapping("/chat.read")
    public void markAsRead(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Header("messageId") Long messageId) {

        chatService.markConversationAsRead(String.valueOf(messageId), userPrincipal.getId());
    }
}