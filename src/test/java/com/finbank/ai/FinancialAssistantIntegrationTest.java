package com.finbank.ai;

import com.finbank.repository.*;
import com.finbank.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("local")
class FinancialAssistantIntegrationTest {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockitoBean
    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        when(currentUserService.getCurrentCustomerId())
                .thenReturn(1L);
    }

    @Test
    void assistant_shouldRetrieveMyAccounts() {

        String response = chatClient
                .prompt()
                .advisors(advisor ->
                        advisor.param(
                                ChatMemory.CONVERSATION_ID,
                                "integration-accounts-test"
                        )
                )
                .user("What are my accounts and their balances?")
                .call()
                .content();

        assertNotNull(response);
        assertFalse(response.isBlank());

        System.out.println("\n=== ACCOUNTS TEST ===");
        System.out.println(response);
    }

    @Test
    void assistant_shouldRetrieveRecentTransactions() {

        String response = chatClient
                .prompt()
                .advisors(advisor ->
                        advisor.param(
                                ChatMemory.CONVERSATION_ID,
                                "integration-transactions-test"
                        )
                )
                .user("Show me my recent transactions.")
                .call()
                .content();

        assertNotNull(response);
        assertFalse(response.isBlank());

        System.out.println("\n=== TRANSACTIONS TEST ===");
        System.out.println(response);
    }

    @Test
    void assistant_shouldAnswerGeneralFinancialQuestion() {

        String response = chatClient
                .prompt()
                .advisors(advisor ->
                        advisor.param(
                                ChatMemory.CONVERSATION_ID,
                                "integration-general-test"
                        )
                )
                .user("What is the difference between a savings account and a current account?")
                .call()
                .content();

        assertNotNull(response);
        assertFalse(response.isBlank());

        System.out.println("\n=== GENERAL QUESTION TEST ===");
        System.out.println(response);
    }
}