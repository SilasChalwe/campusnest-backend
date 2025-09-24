package com.nextinnomind.campusnestbackend.controller;

import com.nextinnomind.campusnestbackend.dto.chat.*;
import com.nextinnomind.campusnestbackend.dto.common.ApiResponse;
import com.nextinnomind.campusnestbackend.security.CurrentUser;
import com.nextinnomind.campusnestbackend.security.UserPrincipal;
import com.nextinnomind.campusnestbackend.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Messaging APIs")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/start")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Start new conversation")
    public ResponseEntity<ApiResponse<ConversationResponse>> startConversation(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody StartConversationRequest request) {
        ConversationResponse response = chatService.startConversation(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Conversation started successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getConversations(
            @CurrentUser UserPrincipal userPrincipal,
            @PageableDefault(size = 20) Pageable pageable) {

        List<ConversationResponse> conversations = chatService.getUserConversations(userPrincipal.getId(), pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Conversations retrieved successfully", conversations)
        );
    }


    @GetMapping("/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get conversation messages")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessages(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long conversationId,
            @PageableDefault(size = 50) Pageable pageable) {
        Page<MessageResponse> messages = chatService.getMessages(String.valueOf(conversationId), userPrincipal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Messages retrieved successfully", messages));
    }

    @PostMapping("/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Send message")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = chatService.sendMessage(String.valueOf(conversationId), request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent successfully", response));
    }

    @PutMapping("/messages/{messageId}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark message as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long messageId) {
        chatService.markConversationAsRead(String.valueOf(messageId), userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Message marked as read", null));
    }
}