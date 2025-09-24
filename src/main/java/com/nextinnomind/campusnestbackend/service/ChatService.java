package com.nextinnomind.campusnestbackend.service;

import com.nextinnomind.campusnestbackend.dto.chat.*;
import com.nextinnomind.campusnestbackend.entity.BookingRequest;
import com.nextinnomind.campusnestbackend.entity.Message;
import com.nextinnomind.campusnestbackend.entity.User;
import com.nextinnomind.campusnestbackend.exception.BadRequestException;
import com.nextinnomind.campusnestbackend.exception.ResourceNotFoundException;
import com.nextinnomind.campusnestbackend.repository.MessageRepository;
import com.nextinnomind.campusnestbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;


import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(Long userId, Pageable pageable) {
        List<String> conversationIds = messageRepository.findConversationsByUserId(userId);

        return conversationIds.stream()
                .map(conversationId -> {
                    // Get the latest message for each conversation
                    Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(
                            conversationId, Pageable.ofSize(1));

                    if (!messages.isEmpty()) {
                        Message latestMessage = messages.getContent().get(0);
                        User otherUser = latestMessage.getSender().getId().equals(userId)
                                ? latestMessage.getReceiver() : latestMessage.getSender();

                        Long unreadCount = messageRepository.countUnreadMessagesByReceiver(userId);

                        return ConversationResponse.builder()
                                .conversationId(conversationId)
                                .otherUser(ConversationResponse.UserInfo.builder()
                                        .id(otherUser.getId())
                                        .fullName(otherUser.getFullName())
                                        .profilePictureUrl(otherUser.getProfilePictureUrl())
                                        .role(otherUser.getRole().name())
                                        .build())
                                .lastMessage(MessageResponse.builder()
                                        .id(latestMessage.getId())
                                        .content(latestMessage.getContent())
                                        .senderId(latestMessage.getSender().getId())
                                        .isRead(latestMessage.getIsRead())
                                        .createdAt(latestMessage.getCreatedAt())
                                        .build())
                                .unreadCount(unreadCount.intValue())
                                .build();
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList()); // no cast needed
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(String conversationId, Long userId, Pageable pageable) {
        // Verify user has access to this conversation
        List<String> userConversations = messageRepository.findConversationsByUserId(userId);
        if (!userConversations.contains(conversationId)) {
            throw new BadRequestException("You don't have access to this conversation");
        }

        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
                .map(this::convertToMessageResponse);
    }

    public ConversationResponse startConversation(StartConversationRequest request, Long senderId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        if (senderId.equals(request.getReceiverId())) {
            throw new BadRequestException("Cannot start conversation with yourself");
        }

        // Generate conversation ID
        String conversationId = generateConversationId(senderId, request.getReceiverId());

        // Send initial message if provided
        if (request.getInitialMessage() != null && !request.getInitialMessage().trim().isEmpty()) {
            SendMessageRequest messageRequest = new SendMessageRequest();
            messageRequest.setContent(request.getInitialMessage());
            sendMessage(conversationId, messageRequest, senderId);
        }

        return ConversationResponse.builder()
                .conversationId(conversationId)
                .otherUser(ConversationResponse.UserInfo.builder()
                        .id(receiver.getId())
                        .fullName(receiver.getFullName())
                        .profilePictureUrl(receiver.getProfilePictureUrl())
                        .role(receiver.getRole().name())
                        .build())
                .unreadCount(0)
                .build();
    }

    public MessageResponse sendMessage(String conversationId, SendMessageRequest request, Long senderId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        // Extract receiver ID from conversation ID or find from existing messages
        Long receiverId = extractReceiverIdFromConversation(conversationId, senderId);
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .conversationId(conversationId)
                .build();

        Message savedMessage = messageRepository.save(message);

        MessageResponse messageResponse = convertToMessageResponse(savedMessage);

        // Send real-time notification
        messagingTemplate.convertAndSendToUser(
                receiver.getId().toString(),
                "/queue/messages",
                messageResponse
        );

        log.info("Message sent from user {} to user {} in conversation {}",
                senderId, receiverId, conversationId);

        return messageResponse;
    }

    public void markConversationAsRead(String conversationId, Long userId) {
        messageRepository.markConversationAsRead(conversationId, userId);
        log.info("Marked conversation {} as read by user {}", conversationId, userId);
    }

    public void createBookingConversation(BookingRequest booking) {
        String conversationId = generateConversationId(
                booking.getStudent().getId(),
                booking.getProperty().getOwner().getId()
        );

        String initialMessage = String.format(
                "Hello! I'm interested in booking your property '%s' for the period %s to %s. %s",
                booking.getProperty().getTitle(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStudentMessage() != null ? booking.getStudentMessage() : ""
        );

        Message message = Message.builder()
                .sender(booking.getStudent())
                .receiver(booking.getProperty().getOwner())
                .content(initialMessage)
                .conversationId(conversationId)
                .build();

        messageRepository.save(message);

        // Notify landlord
        messagingTemplate.convertAndSendToUser(
                booking.getProperty().getOwner().getId().toString(),
                "/queue/messages",
                convertToMessageResponse(message)
        );
    }

    private String generateConversationId(Long userId1, Long userId2) {
        // Generate consistent conversation ID regardless of order
        long smaller = Math.min(userId1, userId2);
        long larger = Math.max(userId1, userId2);
        return "conv_" + smaller + "_" + larger;
    }

    private Long extractReceiverIdFromConversation(String conversationId, Long senderId) {
        // Parse conversation ID to get the other user ID
        String[] parts = conversationId.split("_");
        if (parts.length == 3) {
            Long id1 = Long.parseLong(parts[1]);
            Long id2 = Long.parseLong(parts[2]);
            return id1.equals(senderId) ? id2 : id1;
        }

        // Fallback: find from existing messages
        Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(
                conversationId, Pageable.ofSize(1));
        if (!messages.isEmpty()) {
            Message lastMessage = messages.getContent().get(0);
            return lastMessage.getSender().getId().equals(senderId)
                    ? lastMessage.getReceiver().getId() : lastMessage.getSender().getId();
        }

        throw new BadRequestException("Invalid conversation");
    }

    private MessageResponse convertToMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .receiverId(message.getReceiver().getId())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
}