package com.finbank.controller;

import com.finbank.dto.AssistantChatResponseDto;
import com.finbank.service.FinancialAssistantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class FinancialAssistantControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinancialAssistantService financialAssistantService;

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void chat_shouldRejectMissingMessage() throws Exception {

        mockMvc.perform(
                        post("/api/assistant/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "conversationId": "conversation-123"
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(financialAssistantService);
    }


    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void chat_shouldReturnAssistantResponseForCustomer() throws Exception {

        when(financialAssistantService.chat(
                "What are my accounts?",
                null
        )).thenReturn(
                new AssistantChatResponseDto(
                        "test-conversation-id",
                        "You have one savings account."
                )
        );

        mockMvc.perform(
                        post("/api/assistant/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "message": "What are my accounts?"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId")
                        .value("test-conversation-id"))
                .andExpect(jsonPath("$.response")
                        .value("You have one savings account."));

        verify(financialAssistantService)
                .chat(
                        "What are my accounts?",
                        null
                );
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void chat_shouldPreserveConversationId() throws Exception {

        when(financialAssistantService.chat(
                "Hello",
                "conversation-123"
        )).thenReturn(
                new AssistantChatResponseDto(
                        "conversation-123",
                        "Hello! How can I help you?"
                )
        );

        mockMvc.perform(
                        post("/api/assistant/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "message": "Hello",
                                    "conversationId": "conversation-123"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId")
                        .value("conversation-123"))
                .andExpect(jsonPath("$.response")
                        .value("Hello! How can I help you?"));

        verify(financialAssistantService)
                .chat(
                        "Hello",
                        "conversation-123"
                );
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void chat_shouldRejectBlankMessage() throws Exception {

        mockMvc.perform(
                        post("/api/assistant/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "message": ""
                                }
                                """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(financialAssistantService);
    }

    @Test
    @WithMockUser(
            username = "customer@example.com",
            roles = "CUSTOMER"
    )
    void chat_shouldRejectWhitespaceMessage() throws Exception {

        mockMvc.perform(
                        post("/api/assistant/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "message": "   "
                                }
                                """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(financialAssistantService);
    }

    @Test
    @WithMockUser(
            username = "employee@example.com",
            roles = "EMPLOYEE"
    )
    void chat_shouldRejectNonCustomer() throws Exception {

        mockMvc.perform(
                        post("/api/assistant/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "message": "What are my accounts?"
                                }
                                """)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(financialAssistantService);
    }

    @Test
    void chat_shouldRejectUnauthenticatedUser() throws Exception {

        mockMvc.perform(
                        post("/api/assistant/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "message": "What are my accounts?"
                                }
                                """)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(financialAssistantService);
    }
}