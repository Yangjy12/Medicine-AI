package com.xinglin.ai.controller;

import com.xinglin.ai.common.ApiResponse;
import com.xinglin.ai.common.PageResponse;
import com.xinglin.ai.dto.AskRequest;
import com.xinglin.ai.dto.CreateConversationRequest;
import com.xinglin.ai.security.AiAuthService;
import com.xinglin.ai.security.AuthenticatedUser;
import com.xinglin.ai.service.AiAssistantService;
import com.xinglin.ai.vo.AiAnswerVO;
import com.xinglin.ai.vo.AiConversationVO;
import com.xinglin.ai.vo.AiMessageVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiAssistantService assistantService;
    private final AiAuthService authService;

    public AiController(AiAssistantService assistantService, AiAuthService authService) {
        this.assistantService = assistantService;
        this.authService = authService;
    }

    @GetMapping("/conversations")
    public ApiResponse<PageResponse<AiConversationVO>> conversations(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                                     @RequestParam(defaultValue = "1") Integer page,
                                                                     @RequestParam(defaultValue = "20") Integer pageSize) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(assistantService.listConversations(user.getUserId(), page, pageSize));
    }

    @PostMapping("/conversations")
    public ApiResponse<AiConversationVO> createConversation(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                           @Validated @RequestBody CreateConversationRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(assistantService.createConversation(user.getUserId(), request));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ApiResponse<PageResponse<AiMessageVO>> messages(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                           @PathVariable Long conversationId,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "50") Integer pageSize) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(assistantService.listMessages(user.getUserId(), conversationId, page, pageSize));
    }

    @PostMapping("/ask")
    public ApiResponse<AiAnswerVO> ask(@RequestHeader(value = "Authorization", required = false) String authorization,
                                       @Validated @RequestBody AskRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(assistantService.ask(user.getUserId(), request));
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ApiResponse<Void> deleteConversation(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                @PathVariable Long conversationId) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        assistantService.deleteConversation(user.getUserId(), conversationId);
        return ApiResponse.success(null);
    }
}
