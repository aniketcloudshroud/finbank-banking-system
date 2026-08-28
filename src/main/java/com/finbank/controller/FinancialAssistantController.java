package com.finbank.controller;

import com.finbank.dto.AssistantChatRequestDto;
import com.finbank.dto.AssistantChatResponseDto;
import com.finbank.service.FinancialAssistantService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
@PreAuthorize("hasRole('CUSTOMER')")
public class FinancialAssistantController {

    private final FinancialAssistantService financialAssistantService;

    public FinancialAssistantController(
            FinancialAssistantService financialAssistantService
    ) {
        this.financialAssistantService =
                financialAssistantService;
    }

    @PostMapping("/chat")
    public AssistantChatResponseDto chat(
            @Valid @RequestBody AssistantChatRequestDto request
    ) {

        return financialAssistantService.chat(
                request.getMessage(),
                request.getConversationId()
        );
    }
}