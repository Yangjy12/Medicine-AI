package com.xinglin.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinglin.chat.common.BusinessException;
import com.xinglin.chat.common.PageResponse;
import com.xinglin.chat.dto.AddMembersRequest;
import com.xinglin.chat.dto.CreateGroupConversationRequest;
import com.xinglin.chat.dto.CreatePrivateConversationRequest;
import com.xinglin.chat.dto.ReadConversationRequest;
import com.xinglin.chat.dto.SendMessageRequest;
import com.xinglin.chat.dto.TransferOwnerRequest;
import com.xinglin.chat.dto.UpdateGroupRequest;
import com.xinglin.chat.entity.AppUserSummary;
import com.xinglin.chat.entity.ChatConversation;
import com.xinglin.chat.entity.ChatConversationMember;
import com.xinglin.chat.entity.ChatMessage;
import com.xinglin.chat.repository.ChatConversationMemberRepository;
import com.xinglin.chat.repository.ChatConversationRepository;
import com.xinglin.chat.repository.ChatMessageRepository;
import com.xinglin.chat.vo.ConversationVO;
import com.xinglin.chat.vo.MemberVO;
import com.xinglin.chat.vo.MessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final String PRIVATE = "PRIVATE";
    private static final String GROUP = "GROUP";
    private static final String ACTIVE = "ACTIVE";
    private static final String LEFT = "LEFT";
    private static final String INACTIVE = "INACTIVE";
    private static final String OWNER = "OWNER";
    private static final String MEMBER = "MEMBER";
    private static final String NORMAL = "NORMAL";
    private static final String RECALLED = "RECALLED";

    private final ChatConversationRepository conversationRepository;
    private final ChatConversationMemberRepository memberRepository;
    private final ChatMessageRepository messageRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final UserDirectoryService userDirectoryService;

    @Value("${xinglin.chat.page-size-max:50}")
    private int pageSizeMax;
    @Value("${xinglin.chat.recent-message-ttl-days:7}")
    private int recentMessageTtlDays;
    @Value("${xinglin.chat.max-text-length:2000}")
    private int maxTextLength;

    public ChatService(ChatConversationRepository conversationRepository,
                       ChatConversationMemberRepository memberRepository,
                       ChatMessageRepository messageRepository,
                       StringRedisTemplate redisTemplate,
                       ObjectMapper objectMapper,
                       UserDirectoryService userDirectoryService) {
        this.conversationRepository = conversationRepository;
        this.memberRepository = memberRepository;
        this.messageRepository = messageRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.userDirectoryService = userDirectoryService;
    }

    public PageResponse<ConversationVO> listConversations(Long userId, Integer pageValue, Integer pageSizeValue) {
        int page = normalizePage(pageValue);
        int pageSize = normalizePageSize(pageSizeValue);
        Page<ChatConversationMember> result = memberRepository.findByUserIdAndStatusOrderByJoinedAtDesc(
                userId, ACTIVE, PageRequest.of(page - 1, pageSize));
        List<ConversationVO> records = result.getContent().stream()
                .map(member -> conversationRepository.findById(member.getConversationId())
                        .filter(conversation -> ACTIVE.equals(conversation.getStatus()))
                        .map(conversation -> toConversation(conversation, member, userId))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        log.info("chat conversation list userId={} page={} pageSize={} total={}", userId, page, pageSize, result.getTotalElements());
        return new PageResponse<>(records, page, pageSize, result.getTotalElements());
    }

    @Transactional
    public ConversationVO createPrivateConversation(Long userId, CreatePrivateConversationRequest request) {
        Long targetUserId = request.getTargetUserId();
        if (targetUserId == null || targetUserId <= 0) {
            throw new BusinessException(400, "聊天对象不合法");
        }
        if (userId.equals(targetUserId)) {
            throw new BusinessException(400, "不能和自己创建会话");
        }
        userDirectoryService.requireNormalUser(targetUserId);
        Long minUserId = Math.min(userId, targetUserId);
        Long maxUserId = Math.max(userId, targetUserId);
        ChatConversation conversation = conversationRepository
                .findByConversationTypeAndMinUserIdAndMaxUserIdAndStatus(PRIVATE, minUserId, maxUserId, ACTIVE)
                .orElseGet(() -> createPrivate(userId, targetUserId, minUserId, maxUserId));
        ChatConversationMember member = requireMember(conversation.getId(), userId);
        log.info("chat private conversation ready userId={} targetUserId={} conversationId={}", userId, targetUserId, conversation.getId());
        return toConversation(conversation, member, userId);
    }

    @Transactional
    public ConversationVO createGroupConversation(Long userId, CreateGroupConversationRequest request) {
        ChatConversation conversation = new ChatConversation();
        conversation.setConversationType(GROUP);
        conversation.setTitle(cleanText(request.getTitle(), 64));
        conversation.setStatus(ACTIVE);
        ChatConversation saved = conversationRepository.save(conversation);

        addMember(saved.getId(), userId, OWNER);
        Set<Long> members = new LinkedHashSet<>(request.getMemberIds() == null ? Collections.emptyList() : request.getMemberIds());
        members.remove(userId);
        userDirectoryService.requireNormalUsers(members);
        for (Long memberId : members) {
            if (memberId != null && memberId > 0) {
                addMember(saved.getId(), memberId, MEMBER);
            }
        }
        ChatConversationMember owner = requireMember(saved.getId(), userId);
        log.info("chat group created ownerId={} conversationId={} memberCount={}", userId, saved.getId(), members.size() + 1);
        return toConversation(saved, owner, userId);
    }

    public List<MemberVO> listMembers(Long userId, Long conversationId) {
        requireMember(conversationId, userId);
        return memberRepository.findByConversationIdAndStatus(conversationId, ACTIVE)
                .stream()
                .map(this::toMember)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConversationVO updateGroup(Long userId, Long conversationId, UpdateGroupRequest request) {
        ChatConversation conversation = requireGroupConversation(conversationId);
        ChatConversationMember operator = requireOwner(conversationId, userId);
        conversation.setTitle(cleanText(request.getTitle(), 64));
        ChatConversation saved = conversationRepository.save(conversation);
        log.info("chat group updated userId={} conversationId={} title={}", userId, conversationId, saved.getTitle());
        return toConversation(saved, operator, userId);
    }

    @Transactional
    public List<MemberVO> addMembers(Long userId, Long conversationId, AddMembersRequest request) {
        requireGroupConversation(conversationId);
        requireOwner(conversationId, userId);
        Set<Long> memberIds = new LinkedHashSet<>(request.getMemberIds() == null ? Collections.emptyList() : request.getMemberIds());
        memberIds.remove(userId);
        userDirectoryService.requireNormalUsers(memberIds);
        for (Long memberId : memberIds) {
            if (memberId != null && memberId > 0) {
                addMember(conversationId, memberId, MEMBER);
            }
        }
        log.info("chat group members added operatorId={} conversationId={} addCount={}", userId, conversationId, memberIds.size());
        return listMembers(userId, conversationId);
    }

    @Transactional
    public void removeMember(Long userId, Long conversationId, Long targetUserId) {
        requireGroupConversation(conversationId);
        requireOwner(conversationId, userId);
        if (userId.equals(targetUserId)) {
            throw new BusinessException(400, "群主不能直接移除自己，请先转让群主或解散会话");
        }
        ChatConversationMember target = requireMember(conversationId, targetUserId);
        if (OWNER.equals(target.getMemberRole())) {
            throw new BusinessException(400, "不能移除群主");
        }
        leaveMember(target);
        log.info("chat group member removed operatorId={} conversationId={} targetUserId={}", userId, conversationId, targetUserId);
    }

    @Transactional
    public void transferOwner(Long userId, Long conversationId, TransferOwnerRequest request) {
        requireGroupConversation(conversationId);
        ChatConversationMember currentOwner = requireOwner(conversationId, userId);
        ChatConversationMember target = requireMember(conversationId, request.getTargetUserId());
        if (userId.equals(target.getUserId())) {
            return;
        }
        currentOwner.setMemberRole(MEMBER);
        target.setMemberRole(OWNER);
        memberRepository.save(currentOwner);
        memberRepository.save(target);
        log.info("chat group owner transferred conversationId={} fromUserId={} toUserId={}",
                conversationId, userId, target.getUserId());
    }

    @Transactional
    public void leaveConversation(Long userId, Long conversationId) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(404, "会话不存在"));
        ChatConversationMember member = requireMember(conversationId, userId);
        if (GROUP.equals(conversation.getConversationType()) && OWNER.equals(member.getMemberRole())) {
            long activeCount = memberRepository.countByConversationIdAndStatus(conversationId, ACTIVE);
            if (activeCount > 1) {
                throw new BusinessException(400, "群主退出前需要先转让群主");
            }
        }
        leaveMember(member);
        if (memberRepository.countByConversationIdAndStatus(conversationId, ACTIVE) == 0L) {
            conversation.setStatus(INACTIVE);
            conversationRepository.save(conversation);
        }
        log.info("chat conversation left userId={} conversationId={}", userId, conversationId);
    }

    public PageResponse<MessageVO> listMessages(Long userId,
                                                Long conversationId,
                                                Long afterSeq,
                                                Integer pageValue,
                                                Integer pageSizeValue) {
        requireMember(conversationId, userId);
        int page = normalizePage(pageValue);
        int pageSize = normalizePageSize(pageSizeValue);
        Page<ChatMessage> result;
        List<MessageVO> records;
        if (afterSeq != null && afterSeq >= 0) {
            result = messageRepository.findByConversationIdAndSeqGreaterThanAndStatusOrderBySeqAsc(
                    conversationId, afterSeq, NORMAL, PageRequest.of(page - 1, pageSize));
            records = result.getContent().stream().map(this::toMessage).collect(Collectors.toList());
        } else {
            result = messageRepository.findByConversationIdAndStatusOrderBySeqDesc(
                    conversationId, NORMAL, PageRequest.of(page - 1, pageSize));
            records = result.getContent().stream().map(this::toMessage).collect(Collectors.toList());
            Collections.reverse(records);
        }
        log.info("chat message list userId={} conversationId={} afterSeq={} page={} pageSize={} total={}",
                userId, conversationId, afterSeq, page, pageSize, result.getTotalElements());
        return new PageResponse<>(records, page, pageSize, result.getTotalElements());
    }

    @Transactional
    public MessageVO sendMessage(Long userId, SendMessageRequest request) {
        Long conversationId = request.getConversationId();
        requireMember(conversationId, userId);
        String clientMsgId = StringUtils.hasText(request.getClientMsgId())
                ? request.getClientMsgId().trim()
                : UUID.randomUUID().toString();
        ChatMessage existing = messageRepository.findBySenderIdAndClientMsgId(userId, clientMsgId).orElse(null);
        if (existing != null) {
            log.info("chat message idempotent hit userId={} conversationId={} clientMsgId={}", userId, conversationId, clientMsgId);
            return toMessage(existing);
        }

        Long seq = redisTemplate.opsForValue().increment(seqKey(conversationId));
        if (seq == null) {
            throw new BusinessException(500, "消息序号生成失败");
        }
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setSeq(seq);
        message.setSenderId(userId);
        message.setClientMsgId(clientMsgId);
        String contentType = normalizeContentType(request.getContentType());
        message.setContentType(contentType);
        message.setContent(cleanMessageContent(contentType, request.getContent()));
        message.setMediaUrl(cleanMediaUrl(contentType, request.getMediaUrl()));
        message.setMediaObjectKey(cleanNullable(request.getMediaObjectKey(), 256));
        message.setStatus(NORMAL);
        try {
            ChatMessage saved = messageRepository.save(message);
            updateConversationLastMessage(conversationId, saved);
            increaseUnread(conversationId, userId, saved.getSeq());
            MessageVO vo = toMessage(saved);
            cacheRecentMessage(vo);
            log.info("chat message sent userId={} conversationId={} messageId={} seq={}", userId, conversationId, saved.getId(), saved.getSeq());
            return vo;
        } catch (DataIntegrityViolationException ex) {
            ChatMessage duplicated = messageRepository.findBySenderIdAndClientMsgId(userId, clientMsgId)
                    .orElseThrow(() -> ex);
            return toMessage(duplicated);
        }
    }

    @Transactional
    public void markRead(Long userId, Long conversationId, ReadConversationRequest request) {
        ChatConversationMember member = requireMember(conversationId, userId);
        Long lastReadSeq = request.getLastReadSeq() == null ? 0L : Math.max(0L, request.getLastReadSeq());
        if (lastReadSeq > nonNull(member.getLastReadSeq())) {
            member.setLastReadSeq(lastReadSeq);
            memberRepository.save(member);
        }
        redisTemplate.opsForHash().delete(unreadKey(userId), String.valueOf(conversationId));
        redisTemplate.opsForValue().set(readSeqKey(conversationId, userId), String.valueOf(lastReadSeq), Duration.ofDays(30));
        log.info("chat conversation read userId={} conversationId={} lastReadSeq={}", userId, conversationId, lastReadSeq);
    }

    @Transactional
    public MessageVO recallMessage(Long userId, Long messageId) {
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(404, "消息不存在"));
        requireMember(message.getConversationId(), userId);
        if (!userId.equals(message.getSenderId())) {
            throw new BusinessException(403, "只能撤回自己发送的消息");
        }
        message.setStatus(RECALLED);
        message.setContent("消息已撤回");
        message.setMediaUrl(null);
        message.setMediaObjectKey(null);
        message.setRecalledAt(LocalDateTime.now());
        ChatMessage saved = messageRepository.save(message);
        log.info("chat message recalled userId={} conversationId={} messageId={}", userId, saved.getConversationId(), saved.getId());
        return toMessage(saved);
    }

    public List<Long> activeMemberUserIds(Long conversationId) {
        return memberRepository.findByConversationIdAndStatus(conversationId, ACTIVE)
                .stream()
                .map(ChatConversationMember::getUserId)
                .collect(Collectors.toList());
    }

    private ChatConversation createPrivate(Long userId, Long targetUserId, Long minUserId, Long maxUserId) {
        ChatConversation conversation = new ChatConversation();
        conversation.setConversationType(PRIVATE);
        conversation.setTitle("私聊");
        conversation.setMinUserId(minUserId);
        conversation.setMaxUserId(maxUserId);
        conversation.setStatus(ACTIVE);
        ChatConversation saved = conversationRepository.save(conversation);
        addMember(saved.getId(), userId, MEMBER);
        addMember(saved.getId(), targetUserId, MEMBER);
        return saved;
    }

    private void addMember(Long conversationId, Long userId, String role) {
        ChatConversationMember existing = memberRepository.findByConversationIdAndUserId(conversationId, userId).orElse(null);
        if (existing != null && ACTIVE.equals(existing.getStatus())) {
            return;
        }
        if (existing != null) {
            existing.setMemberRole(role);
            existing.setStatus(ACTIVE);
            existing.setLeftAt(null);
            existing.setJoinedAt(LocalDateTime.now());
            memberRepository.save(existing);
            redisTemplate.opsForHash().delete(unreadKey(userId), String.valueOf(conversationId));
            return;
        }
        ChatConversationMember member = new ChatConversationMember();
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setMemberRole(role);
        member.setStatus(ACTIVE);
        memberRepository.save(member);
    }

    private void leaveMember(ChatConversationMember member) {
        member.setStatus(LEFT);
        member.setLeftAt(LocalDateTime.now());
        memberRepository.save(member);
        redisTemplate.opsForHash().delete(unreadKey(member.getUserId()), String.valueOf(member.getConversationId()));
    }

    private ChatConversationMember requireMember(Long conversationId, Long userId) {
        if (conversationId == null || conversationId <= 0) {
            throw new BusinessException(400, "会话ID不合法");
        }
        return memberRepository.findByConversationIdAndUserId(conversationId, userId)
                .filter(member -> ACTIVE.equals(member.getStatus()))
                .orElseThrow(() -> new BusinessException(403, "无权访问该会话"));
    }

    private ChatConversation requireGroupConversation(Long conversationId) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .filter(value -> ACTIVE.equals(value.getStatus()))
                .orElseThrow(() -> new BusinessException(404, "会话不存在"));
        if (!GROUP.equals(conversation.getConversationType())) {
            throw new BusinessException(400, "该操作仅支持群聊");
        }
        return conversation;
    }

    private ChatConversationMember requireOwner(Long conversationId, Long userId) {
        ChatConversationMember member = requireMember(conversationId, userId);
        if (!OWNER.equals(member.getMemberRole())) {
            throw new BusinessException(403, "只有群主可以执行该操作");
        }
        return member;
    }

    private void updateConversationLastMessage(Long conversationId, ChatMessage message) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(404, "会话不存在"));
        conversation.setLastMessageId(message.getId());
        conversation.setLastMessagePreview(messagePreview(message));
        conversation.setLastMessageTime(message.getSentAt());
        conversationRepository.save(conversation);
    }

    private void increaseUnread(Long conversationId, Long senderId, Long seq) {
        List<ChatConversationMember> members = memberRepository.findByConversationIdAndStatus(conversationId, ACTIVE);
        for (ChatConversationMember member : members) {
            if (senderId.equals(member.getUserId())) {
                member.setLastReadSeq(seq);
                memberRepository.save(member);
                continue;
            }
            redisTemplate.opsForHash().increment(unreadKey(member.getUserId()), String.valueOf(conversationId), 1L);
        }
    }

    private void cacheRecentMessage(MessageVO message) {
        String key = recentMessageKey(message.getConversationId());
        try {
            redisTemplate.opsForZSet().add(key, objectMapper.writeValueAsString(message), message.getSeq());
            redisTemplate.expire(key, Duration.ofDays(recentMessageTtlDays));
        } catch (JsonProcessingException ex) {
            log.warn("chat recent message cache failed messageId={} error={}", message.getId(), ex.getMessage());
        }
    }

    private ConversationVO toConversation(ChatConversation conversation, ChatConversationMember member, Long currentUserId) {
        ConversationVO vo = new ConversationVO();
        vo.setId(conversation.getId());
        vo.setConversationType(conversation.getConversationType());
        vo.setTitle(resolveTitle(conversation, currentUserId));
        fillConversationAvatar(vo, conversation, currentUserId);
        vo.setLastMessagePreview(conversation.getLastMessagePreview());
        vo.setLastMessageTime(conversation.getLastMessageTime());
        vo.setLastReadSeq(nonNull(member.getLastReadSeq()));
        vo.setUnreadCount(parseLong(redisTemplate.opsForHash().get(unreadKey(currentUserId), String.valueOf(conversation.getId()))));
        vo.setCurrentUserRole(member.getMemberRole());
        vo.setMemberCount(memberRepository.countByConversationIdAndStatus(conversation.getId(), ACTIVE));
        return vo;
    }

    private MemberVO toMember(ChatConversationMember member) {
        MemberVO vo = new MemberVO();
        vo.setUserId(member.getUserId());
        AppUserSummary user = userDirectoryService.findNormalUsers(Collections.singleton(member.getUserId())).get(member.getUserId());
        vo.setNickname(userDirectoryService.displayName(user));
        vo.setAvatar(user == null ? null : user.getAvatar());
        vo.setMemberRole(member.getMemberRole());
        vo.setLastReadSeq(nonNull(member.getLastReadSeq()));
        vo.setStatus(member.getStatus());
        vo.setJoinedAt(member.getJoinedAt());
        vo.setLeftAt(member.getLeftAt());
        return vo;
    }

    private MessageVO toMessage(ChatMessage message) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setConversationId(message.getConversationId());
        vo.setSeq(message.getSeq());
        vo.setSenderId(message.getSenderId());
        AppUserSummary sender = userDirectoryService.findNormalUsers(Collections.singleton(message.getSenderId())).get(message.getSenderId());
        vo.setSenderName(userDirectoryService.displayName(sender));
        vo.setSenderAvatar(sender == null ? null : sender.getAvatar());
        vo.setClientMsgId(message.getClientMsgId());
        vo.setContentType(message.getContentType());
        vo.setContent(message.getContent());
        vo.setMediaUrl(message.getMediaUrl());
        vo.setMediaObjectKey(message.getMediaObjectKey());
        vo.setStatus(message.getStatus());
        vo.setSentAt(message.getSentAt());
        return vo;
    }

    private String resolveTitle(ChatConversation conversation, Long currentUserId) {
        if (GROUP.equals(conversation.getConversationType()) || StringUtils.hasText(conversation.getTitle()) && !"私聊".equals(conversation.getTitle())) {
            return conversation.getTitle();
        }
        Long targetId = Objects.equals(currentUserId, conversation.getMinUserId()) ? conversation.getMaxUserId() : conversation.getMinUserId();
        return targetId == null ? "私聊" : userDirectoryService.displayName(targetId);
    }

    private void fillConversationAvatar(ConversationVO vo, ChatConversation conversation, Long currentUserId) {
        if (GROUP.equals(conversation.getConversationType())) {
            vo.setAvatar(conversation.getAvatar());
            return;
        }
        Long targetId = Objects.equals(currentUserId, conversation.getMinUserId()) ? conversation.getMaxUserId() : conversation.getMinUserId();
        if (targetId == null) {
            return;
        }
        Map<Long, AppUserSummary> users = userDirectoryService.findNormalUsers(Collections.singleton(targetId));
        AppUserSummary target = users.get(targetId);
        vo.setTargetUserId(targetId);
        vo.setTargetUserName(userDirectoryService.displayName(target));
        vo.setTargetUserAvatar(target == null ? null : target.getAvatar());
        vo.setAvatar(vo.getTargetUserAvatar());
    }

    private String normalizeContentType(String value) {
        if (!StringUtils.hasText(value)) {
            return "TEXT";
        }
        String type = value.trim().toUpperCase();
        if (!"TEXT".equals(type) && !"IMAGE".equals(type) && !"VIDEO".equals(type) && !"FILE".equals(type)) {
            throw new BusinessException(400, "消息类型不支持");
        }
        return type;
    }

    private String cleanText(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, "内容不能为空");
        }
        String clean = value.trim();
        return clean.length() > maxLength ? clean.substring(0, maxLength) : clean;
    }

    private String cleanMessageContent(String contentType, String value) {
        if ("TEXT".equals(contentType)) {
            return cleanText(value, maxTextLength);
        }
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String clean = value.trim();
        return clean.length() > maxTextLength ? clean.substring(0, maxTextLength) : clean;
    }

    private String cleanMediaUrl(String contentType, String mediaUrl) {
        if ("TEXT".equals(contentType)) {
            return cleanNullable(mediaUrl, 512);
        }
        if (!StringUtils.hasText(mediaUrl)) {
            throw new BusinessException(400, "媒体消息地址不能为空");
        }
        return cleanNullable(mediaUrl, 512);
    }

    private String cleanNullable(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String clean = value.trim();
        return clean.length() > maxLength ? clean.substring(0, maxLength) : clean;
    }

    private String summary(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String clean = value.trim().replaceAll("\\s+", " ");
        return clean.length() <= 80 ? clean : clean.substring(0, 80) + "...";
    }

    private String messagePreview(ChatMessage message) {
        if ("TEXT".equals(message.getContentType()) || StringUtils.hasText(message.getContent())) {
            return summary(message.getContent());
        }
        if ("IMAGE".equals(message.getContentType())) {
            return "[图片]";
        }
        if ("VIDEO".equals(message.getContentType())) {
            return "[视频]";
        }
        if ("FILE".equals(message.getContentType())) {
            return "[文件]";
        }
        return "[消息]";
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, pageSizeMax);
    }

    private long nonNull(Long value) {
        return value == null ? 0L : value;
    }

    private long parseLong(Object value) {
        try {
            return value == null ? 0L : Long.parseLong(String.valueOf(value));
        } catch (Exception ex) {
            return 0L;
        }
    }

    private String seqKey(Long conversationId) {
        return "chat:conversation:seq:" + conversationId;
    }

    private String unreadKey(Long userId) {
        return "chat:unread:" + userId;
    }

    private String readSeqKey(Long conversationId, Long userId) {
        return "chat:read:seq:" + conversationId + ":" + userId;
    }

    private String recentMessageKey(Long conversationId) {
        return "chat:message:zset:" + conversationId;
    }
}
