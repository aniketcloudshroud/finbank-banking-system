package com.finbank.dto;

public class AssistantChatResponseDto {

    private String conversationId;
    private String response;

    public AssistantChatResponseDto() {
    }

    public AssistantChatResponseDto(
            String conversationId,
            String response
    ) {
        this.conversationId = conversationId;
        this.response = response;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}