package com.finbank.dto;

import jakarta.validation.constraints.NotBlank;

public class AssistantChatRequestDto {

    @NotBlank
    private String message;

    private String conversationId;

    public AssistantChatRequestDto() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}