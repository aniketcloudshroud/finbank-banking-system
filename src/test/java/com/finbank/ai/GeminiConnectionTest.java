package com.finbank.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class GeminiConnectionTest {

    @Autowired
    private ChatClient chatClient;

    @Test
    void shouldConnectToGemini() {

        String response = chatClient
                .prompt()
                .advisors(advisor ->
                        advisor.param(
                                ChatMemory.CONVERSATION_ID,
                                "gemini-connection-test"
                        )
                )
                .user("Reply with exactly: GEMINI_OK")
                .call()
                .content();

        assertNotNull(response);
        assertFalse(response.isBlank());

        System.out.println("Gemini response: " + response);
    }
}