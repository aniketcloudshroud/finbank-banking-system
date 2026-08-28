package com.finbank.ai;

import org.springframework.security.test.context.support.WithMockUser;
import com.finbank.dto.AssistantChatResponseDto;
import com.finbank.service.CurrentUserService;
import com.finbank.service.FinancialAssistantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("local")
class FinancialAssistantMemoryIntegrationTest {

    private static final String CLIENT_CONVERSATION_ID =
            "memory-integration-test";

    private static final String SCOPED_CONVERSATION_ID =
            "customer-1-conversation-memory-integration-test";

    @Autowired
    private FinancialAssistantService financialAssistantService;

    @Autowired
    private ChatMemoryRepository chatMemoryRepository;

    @MockitoBean
    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {

        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);

        chatMemoryRepository.deleteByConversationId(
                SCOPED_CONVERSATION_ID
        );
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void conversationMemory_shouldPersistAcrossMultipleRequests() {

        AssistantChatResponseDto firstResponse =
                financialAssistantService.chat(
                        "Remember this: my favorite color is blue.",
                        CLIENT_CONVERSATION_ID
                );

        assertNotNull(firstResponse);

        assertEquals(
                CLIENT_CONVERSATION_ID,
                firstResponse.getConversationId()
        );

        List<?> messagesAfterFirstRequest =
                chatMemoryRepository.findByConversationId(
                        SCOPED_CONVERSATION_ID
                );

        assertFalse(
                messagesAfterFirstRequest.isEmpty(),
                "First request should be persisted in chat memory"
        );

        int messageCountAfterFirstRequest =
                messagesAfterFirstRequest.size();

        AssistantChatResponseDto secondResponse =
                financialAssistantService.chat(
                        "What color did I just tell you to remember?",
                        CLIENT_CONVERSATION_ID
                );

        assertNotNull(secondResponse);

        assertEquals(
                CLIENT_CONVERSATION_ID,
                secondResponse.getConversationId()
        );

        List<?> messagesAfterSecondRequest =
                chatMemoryRepository.findByConversationId(
                        SCOPED_CONVERSATION_ID
                );

        assertTrue(
                messagesAfterSecondRequest.size()
                        > messageCountAfterFirstRequest,
                "Second request should add messages to the same conversation"
        );

        assertTrue(
                messagesAfterSecondRequest.size() >= 4,
                "Two chat turns should contain at least four messages"
        );

        assertTrue(
                messagesAfterSecondRequest.stream()
                        .anyMatch(message ->
                                message.toString()
                                        .toLowerCase()
                                        .contains("blue")
                        ),
                "Conversation memory should contain the first user message"
        );
    }
}