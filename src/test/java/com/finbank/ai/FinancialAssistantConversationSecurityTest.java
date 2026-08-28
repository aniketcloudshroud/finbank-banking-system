package com.finbank.ai;

import com.finbank.dto.AssistantChatResponseDto;
import com.finbank.service.CurrentUserService;
import com.finbank.service.FinancialAssistantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialAssistantConversationSecurityTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.AdvisorSpec advisorSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private FinancialAssistantService service;

    @BeforeEach
    void setUp() {

        service = new FinancialAssistantService(
                chatClient,
                currentUserService
        );

        SecurityContextHolder.setContext(securityContext);

        when(chatClient.prompt())
                .thenReturn(requestSpec);

        when(requestSpec.user(anyString()))
                .thenReturn(requestSpec);

        when(requestSpec.advisors(any(Consumer.class)))
                .thenReturn(requestSpec);

        when(requestSpec.call())
                .thenReturn(callResponseSpec);

        when(callResponseSpec.content())
                .thenReturn("Test response");
    }

    @Test
    void sameConversationId_shouldBeScopedDifferentlyForDifferentCustomers() {

        String conversationId = "shared-conversation";

        // First customer
        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        service.chat(
                "What are my accounts?",
                conversationId
        );

        ArgumentCaptor<Consumer<ChatClient.AdvisorSpec>> captor1 =
                ArgumentCaptor.forClass(Consumer.class);

        verify(requestSpec)
                .advisors(captor1.capture());

        Consumer<ChatClient.AdvisorSpec> advisorConsumer1 =
                captor1.getValue();

        advisorConsumer1.accept(advisorSpec);

        verify(advisorSpec)
                .param(
                        ChatMemory.CONVERSATION_ID,
                        "customer-1-conversation-shared-conversation"
                );

        reset(requestSpec, advisorSpec);

        when(requestSpec.user(anyString()))
                .thenReturn(requestSpec);

        when(requestSpec.advisors(any(Consumer.class)))
                .thenReturn(requestSpec);

        when(requestSpec.call())
                .thenReturn(callResponseSpec);

        // Second customer
        when(currentUserService.getCurrentCustomerId())
                .thenReturn(2L);

        service.chat(
                "What are my accounts?",
                conversationId
        );

        ArgumentCaptor<Consumer<ChatClient.AdvisorSpec>> captor2 =
                ArgumentCaptor.forClass(Consumer.class);

        verify(requestSpec)
                .advisors(captor2.capture());

        Consumer<ChatClient.AdvisorSpec> advisorConsumer2 =
                captor2.getValue();

        advisorConsumer2.accept(advisorSpec);

        verify(advisorSpec)
                .param(
                        ChatMemory.CONVERSATION_ID,
                        "customer-2-conversation-shared-conversation"
                );

        assertNotEquals(
                "customer-1-conversation-shared-conversation",
                "customer-2-conversation-shared-conversation"
        );
    }

    @Test
    void blankConversationId_shouldGenerateConversationId() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        AssistantChatResponseDto response =
                service.chat(
                        "Hello",
                        "   "
                );

        assertNotNull(response);
        assertNotNull(response.getConversationId());
        assertFalse(response.getConversationId().isBlank());

        verify(requestSpec)
                .advisors(any(Consumer.class));
    }

    @Test
    void nullConversationId_shouldGenerateConversationId() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        AssistantChatResponseDto response =
                service.chat(
                        "Hello",
                        null
                );

        assertNotNull(response);
        assertNotNull(response.getConversationId());
        assertFalse(response.getConversationId().isBlank());

        verify(requestSpec)
                .advisors(any(Consumer.class));
    }

    @Test
    void conversationId_shouldBeTrimmedBeforeScoping() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        service.chat(
                "Hello",
                "   my-conversation-123   "
        );

        ArgumentCaptor<Consumer<ChatClient.AdvisorSpec>> captor =
                ArgumentCaptor.forClass(Consumer.class);

        verify(requestSpec)
                .advisors(captor.capture());

        captor.getValue().accept(advisorSpec);

        verify(advisorSpec)
                .param(
                        ChatMemory.CONVERSATION_ID,
                        "customer-1-conversation-my-conversation-123"
                );
    }
}