package com.xinglin.chat.controller;

import com.xinglin.chat.common.ApiResponse;
import com.xinglin.chat.common.PageResponse;
import com.xinglin.chat.dto.AddMembersRequest;
import com.xinglin.chat.dto.CreateGroupConversationRequest;
import com.xinglin.chat.dto.CreatePrivateConversationRequest;
import com.xinglin.chat.dto.ReadConversationRequest;
import com.xinglin.chat.dto.SendMessageRequest;
import com.xinglin.chat.dto.TransferOwnerRequest;
import com.xinglin.chat.dto.UpdateGroupRequest;
import com.xinglin.chat.security.AuthenticatedUser;
import com.xinglin.chat.security.ChatAuthService;
import com.xinglin.chat.service.ChatService;
import com.xinglin.chat.vo.ConversationVO;
import com.xinglin.chat.vo.MemberVO;
import com.xinglin.chat.vo.MessageVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;
    private final ChatAuthService authService;

    public ChatController(ChatService chatService, ChatAuthService authService) {
        this.chatService = chatService;
        this.authService = authService;
    }

    @GetMapping("/conversations")
    public ApiResponse<PageResponse<ConversationVO>> conversations(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                                   @RequestParam(defaultValue = "1") Integer page,
                                                                   @RequestParam(defaultValue = "20") Integer pageSize) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(chatService.listConversations(user.getUserId(), page, pageSize));
    }

    @PostMapping("/conversations/private")
    public ApiResponse<ConversationVO> createPrivate(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                     @Validated @RequestBody CreatePrivateConversationRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(chatService.createPrivateConversation(user.getUserId(), request));
    }

    @PostMapping("/conversations/group")
    public ApiResponse<ConversationVO> createGroup(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                   @Validated @RequestBody CreateGroupConversationRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(chatService.createGroupConversation(user.getUserId(), request));
    }

    @PutMapping("/conversations/{conversationId}")
    public ApiResponse<ConversationVO> updateGroup(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                   @PathVariable Long conversationId,
                                                   @Validated @RequestBody UpdateGroupRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(chatService.updateGroup(user.getUserId(), conversationId, request));
    }

    @GetMapping("/conversations/{conversationId}/members")
    public ApiResponse<List<MemberVO>> members(@RequestHeader(value = "Authorization", required = false) String authorization,
                                               @PathVariable Long conversationId) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(chatService.listMembers(user.getUserId(), conversationId));
    }

    @PostMapping("/conversations/{conversationId}/members")
    public ApiResponse<List<MemberVO>> addMembers(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  @PathVariable Long conversationId,
                                                  @Validated @RequestBody AddMembersRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(chatService.addMembers(user.getUserId(), conversationId, request));
    }

    @DeleteMapping("/conversations/{conversationId}/members/{memberId}")
    public ApiResponse<Void> removeMember(@RequestHeader(value = "Authorization", required = false) String authorization,
                                          @PathVariable Long conversationId,
                                          @PathVariable Long memberId) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        chatService.removeMember(user.getUserId(), conversationId, memberId);
        return ApiResponse.success(null);
    }

    @PostMapping("/conversations/{conversationId}/owner")
    public ApiResponse<Void> transferOwner(@RequestHeader(value = "Authorization", required = false) String authorization,
                                           @PathVariable Long conversationId,
                                           @Validated @RequestBody TransferOwnerRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        chatService.transferOwner(user.getUserId(), conversationId, request);
        return ApiResponse.success(null);
    }

    @PostMapping("/conversations/{conversationId}/leave")
    public ApiResponse<Void> leaveConversation(@RequestHeader(value = "Authorization", required = false) String authorization,
                                               @PathVariable Long conversationId) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        chatService.leaveConversation(user.getUserId(), conversationId);
        return ApiResponse.success(null);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ApiResponse<PageResponse<MessageVO>> messages(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                         @PathVariable Long conversationId,
                                                         @RequestParam(required = false) Long afterSeq,
                                                         @RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "20") Integer pageSize) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(chatService.listMessages(user.getUserId(), conversationId, afterSeq, page, pageSize));
    }

    @PostMapping("/messages")
    public ApiResponse<MessageVO> sendMessage(@RequestHeader(value = "Authorization", required = false) String authorization,
                                              @Validated @RequestBody SendMessageRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(chatService.sendMessage(user.getUserId(), request));
    }

    @PostMapping("/conversations/{conversationId}/read")
    public ApiResponse<Void> markRead(@RequestHeader(value = "Authorization", required = false) String authorization,
                                      @PathVariable Long conversationId,
                                      @Validated @RequestBody ReadConversationRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        chatService.markRead(user.getUserId(), conversationId, request);
        return ApiResponse.success(null);
    }

    @PostMapping("/messages/{messageId}/recall")
    public ApiResponse<MessageVO> recall(@RequestHeader(value = "Authorization", required = false) String authorization,
                                         @PathVariable Long messageId) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(chatService.recallMessage(user.getUserId(), messageId));
    }
}
