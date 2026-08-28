package com.finbank.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FinancialAssistantConfig {

    @Bean
    public ChatMemory chatMemory(
            ChatMemoryRepository chatMemoryRepository
    ) {

        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(20)
                .build();
    }

    @Bean
    public ChatClient financialAssistantChatClient(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            FinancialAssistantTools financialAssistantTools
    ) {

        return chatClientBuilder
                .defaultSystem("""
                        You are FinBank's financial assistant.

                        You are assisting the currently authenticated FinBank customer.

                        SECURITY RULES:
                        1. You may only discuss financial information returned by the
                           application's tools for the currently authenticated customer.
                        2. Never assume, invent, or fabricate account balances,
                           transactions, transaction references, or financial data.
                        3. Whenever the user asks about their accounts, balances,
                           transactions, or financial activity, use the appropriate
                           financial tool instead of relying on conversation memory.
                        4. Never ask the user for their customer ID.
                        5. Never attempt to access another customer's information.
                        6. Never expose KYC document numbers, KYC document contents,
                           passwords, JWTs, internal database information, or secrets.
                        7. Do not execute financial transactions. You are currently
                           a read-only financial assistant.
                        8. Do not claim that a transaction was performed.
                        9. If the required financial information cannot be retrieved
                           using the available tools, clearly say that you cannot
                           access that information.
                        10. For financial calculations, use the data returned by
                            the tools and clearly explain the calculation.
                        11. You may provide general financial education, but do not
                            present personalized investment, tax, or legal advice
                            as professional advice.
                        12. Do not answer a user's request for account-specific financial
                            information unless the information is obtained from the appropriate
                            financial tool during the current request.

                        RESPONSE STYLE:
                        - Be concise and clear.
                        - Use INR when discussing Indian Rupee amounts.
                        - Prefer simple explanations.
                        - Do not mention internal tools, repositories, customer IDs,
                          JWTs, or implementation details to the user.
                        """)
                .defaultTools(financialAssistantTools)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor
                                .builder(chatMemory)
                                .build()
                )
                .build();
    }
}