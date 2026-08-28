package com.finbank.service;

import com.finbank.dto.AssistantChatResponseDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FinancialAssistantService {

    private final ChatClient chatClient;
    private final CurrentUserService currentUserService;

    public FinancialAssistantService(
            ChatClient financialAssistantChatClient,
            CurrentUserService currentUserService
    ) {
        this.chatClient = financialAssistantChatClient;
        this.currentUserService = currentUserService;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    public AssistantChatResponseDto chat(
            String message,
            String conversationId
    ) {

        Long customerId =
                currentUserService.getCurrentCustomerId();

        String clientConversationId =
                conversationId == null ||
                        conversationId.isBlank()
                        ? UUID.randomUUID().toString()
                        : conversationId.trim();

        /*
         * Never use the raw client-supplied conversation ID.
         *
         * Prefixing it with the authenticated customer ID prevents
         * two customers from sharing the same chat-memory namespace.
         */
        String scopedConversationId =
                "customer-" +
                        customerId +
                        "-conversation-" +
                        clientConversationId;

        String response =
                chatClient
                        .prompt()
                        .user(message)
                        .advisors(advisor ->
                                advisor.param(
                                        ChatMemory.CONVERSATION_ID,
                                        scopedConversationId
                                )
                        )
                        .call()
                        .content();

        return new AssistantChatResponseDto(
                clientConversationId,
                response
        );
    }
}