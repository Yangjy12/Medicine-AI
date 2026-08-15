package com.xinglin.chat.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinglin.chat.common.BusinessException;
import com.xinglin.chat.dto.SendMessageRequest;
import com.xinglin.chat.security.AuthenticatedUser;
import com.xinglin.chat.security.ChatAuthService;
import com.xinglin.chat.service.ChatService;
import com.xinglin.chat.vo.MessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private static final String USER_ID_KEY = "userId";

    private final ChatAuthService authService;
    private final ChatService chatService;
    private final ChatWebSocketSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(ChatAuthService authService,
                                ChatService chatService,
                                ChatWebSocketSessionRegistry sessionRegistry,
                                ObjectMapper objectMapper) {
        this.authService = authService;
        this.chatService = chatService;
        this.sessionRegistry = sessionRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractToken(session.getUri());
        AuthenticatedUser user = authService.requireToken(token);
        session.getAttributes().put(USER_ID_KEY, user.getUserId());
        sessionRegistry.add(user.getUserId(), session);
        session.sendMessage(new TextMessage(event("CONNECTED", "connected")));
        log.info("chat websocket connected userId={} sessionId={}", user.getUserId(), session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Long userId = currentUserId(session);
        JsonNode root = objectMapper.readTree(textMessage.getPayload());
        String type = root.hasNonNull("type") ? root.get("type").asText() : "CHAT_MESSAGE";
        if ("PING".equalsIgnoreCase(type)) {
            session.sendMessage(new TextMessage(event("PONG", "pong")));
            return;
        }
        if (!"CHAT_MESSAGE".equalsIgnoreCase(type)) {
            throw new BusinessException(400, "WebSocket消息类型不支持");
        }
        SendMessageRequest request = parseSendMessage(root);
        MessageVO message = chatService.sendMessage(userId, request);
        String payload = event("CHAT_MESSAGE", message);
        List<Long> memberIds = chatService.activeMemberUserIds(message.getConversationId());
        for (Long memberId : memberIds) {
            sessionRegistry.sendToUser(memberId, payload);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("chat websocket transport error sessionId={} error={}", session.getId(), exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = currentUserId(session);
        if (userId != null) {
            sessionRegistry.remove(userId, session);
        }
        log.info("chat websocket closed userId={} sessionId={} status={}", userId, session.getId(), status);
    }

    private SendMessageRequest parseSendMessage(JsonNode root) {
        JsonNode data = root.has("data") ? root.get("data") : root;
        SendMessageRequest request = new SendMessageRequest();
        if (data.hasNonNull("conversationId")) {
            request.setConversationId(data.get("conversationId").asLong());
        }
        if (data.hasNonNull("clientMsgId")) {
            request.setClientMsgId(data.get("clientMsgId").asText());
        }
        if (data.hasNonNull("contentType")) {
            request.setContentType(data.get("contentType").asText());
        }
        if (data.hasNonNull("content")) {
            request.setContent(data.get("content").asText());
        }
        if (data.hasNonNull("mediaUrl")) {
            request.setMediaUrl(data.get("mediaUrl").asText());
        }
        if (data.hasNonNull("mediaObjectKey")) {
            request.setMediaObjectKey(data.get("mediaObjectKey").asText());
        }
        return request;
    }

    private Long currentUserId(WebSocketSession session) {
        Object value = session.getAttributes().get(USER_ID_KEY);
        return value instanceof Long ? (Long) value : null;
    }

    private String extractToken(URI uri) {
        if (uri == null) {
            return null;
        }
        String token = UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst("token");
        return StringUtils.hasText(token) ? token : null;
    }

    private String event(String type, Object data) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type);
        payload.put("data", data);
        return objectMapper.writeValueAsString(payload);
    }
}
