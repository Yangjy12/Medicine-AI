package com.xinglin.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinglin.ai.common.BusinessException;
import com.xinglin.ai.common.PageResponse;
import com.xinglin.ai.dto.AskRequest;
import com.xinglin.ai.dto.CreateConversationRequest;
import com.xinglin.ai.entity.AiConversation;
import com.xinglin.ai.entity.AiMessage;
import com.xinglin.ai.entity.VideoKnowledge;
import com.xinglin.ai.repository.AiConversationRepository;
import com.xinglin.ai.repository.AiMessageRepository;
import com.xinglin.ai.repository.VideoKnowledgeRepository;
import com.xinglin.ai.vo.AiAnswerVO;
import com.xinglin.ai.vo.AiConversationVO;
import com.xinglin.ai.vo.AiMessageVO;
import com.xinglin.ai.vo.CitationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiAssistantService {
    private static final Logger log = LoggerFactory.getLogger(AiAssistantService.class);
    private static final String ACTIVE = "ACTIVE";
    private static final String DELETED = "DELETED";
    private static final String USER = "USER";
    private static final String ASSISTANT = "ASSISTANT";
    private static final String ONLINE = "ONLINE";
    private static final List<String> DOMAIN_TERMS = Arrays.asList(
            "中医", "阴阳", "五行", "藏象", "经络", "腧穴", "针灸", "艾灸", "方剂", "中药",
            "四诊", "望闻问切", "辨证", "八纲", "脏腑", "气血津液", "病机", "治法",
            "伤寒", "金匮", "温病", "黄帝内经", "本草", "脾胃", "肝肾", "心肺", "入门", "学习计划"
    );

    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;
    private final VideoKnowledgeRepository videoRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final LlmClient llmClient;

    @Value("${xinglin.ai.page-size-max:50}")
    private int pageSizeMax;
    @Value("${xinglin.ai.max-question-length:1000}")
    private int maxQuestionLength;
    @Value("${xinglin.ai.rag-top-k:5}")
    private int ragTopK;
    @Value("${xinglin.ai.rag-candidate-limit:30}")
    private int ragCandidateLimit;
    @Value("${xinglin.ai.rate-limit-per-minute:20}")
    private int rateLimitPerMinute;

    public AiAssistantService(AiConversationRepository conversationRepository,
                              AiMessageRepository messageRepository,
                              VideoKnowledgeRepository videoRepository,
                              StringRedisTemplate redisTemplate,
                              ObjectMapper objectMapper,
                              LlmClient llmClient) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.videoRepository = videoRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.llmClient = llmClient;
    }

    public PageResponse<AiConversationVO> listConversations(Long userId, Integer pageValue, Integer pageSizeValue) {
        int page = normalizePage(pageValue);
        int pageSize = normalizePageSize(pageSizeValue);
        Page<AiConversation> result = conversationRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(
                userId, ACTIVE, PageRequest.of(page - 1, pageSize));
        List<AiConversationVO> records = result.getContent().stream().map(this::toConversation).collect(Collectors.toList());
        return new PageResponse<>(records, page, pageSize, result.getTotalElements());
    }

    @Transactional
    public AiConversationVO createConversation(Long userId, CreateConversationRequest request) {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setTitle(StringUtils.hasText(request.getTitle()) ? cleanText(request.getTitle(), 64) : "新的问答");
        AiConversation saved = conversationRepository.save(conversation);
        log.info("ai conversation created userId={} conversationId={}", userId, saved.getId());
        return toConversation(saved);
    }

    public PageResponse<AiMessageVO> listMessages(Long userId, Long conversationId, Integer pageValue, Integer pageSizeValue) {
        requireConversation(conversationId, userId);
        int page = normalizePage(pageValue);
        int pageSize = normalizePageSize(pageSizeValue);
        Page<AiMessage> result = messageRepository.findByConversationIdAndUserIdOrderByCreatedAtAsc(
                conversationId, userId, PageRequest.of(page - 1, pageSize));
        List<AiMessageVO> records = result.getContent().stream().map(this::toMessage).collect(Collectors.toList());
        return new PageResponse<>(records, page, pageSize, result.getTotalElements());
    }

    @Transactional
    public AiAnswerVO ask(Long userId, AskRequest request) {
        checkRateLimit(userId);
        String question = cleanText(request.getQuestion(), maxQuestionLength);
        AiConversation conversation = request.getConversationId() == null
                ? createConversationEntity(userId, titleFromQuestion(question))
                : requireConversation(request.getConversationId(), userId);

        AiMessage userMessage = saveMessage(conversation.getId(), userId, USER, question, null);
        List<CitationVO> citations = retrieve(question);
        String answer = generateAnswer(question, citations);
        AiMessage assistantMessage = saveMessage(conversation.getId(), userId, ASSISTANT, answer, citations);

        conversation.setLastQuestion(question);
        conversation.setLastAnswerPreview(summary(answer, 300));
        conversation.setMessageCount(nonNull(conversation.getMessageCount()) + 2);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        AiAnswerVO vo = new AiAnswerVO();
        vo.setConversationId(conversation.getId());
        vo.setUserMessageId(userMessage.getId());
        vo.setAssistantMessageId(assistantMessage.getId());
        vo.setAnswer(answer);
        vo.setCitations(citations);
        log.info("ai ask success userId={} conversationId={} userMessageId={} assistantMessageId={} citations={}",
                userId, conversation.getId(), userMessage.getId(), assistantMessage.getId(), citations.size());
        return vo;
    }

    @Transactional
    public void deleteConversation(Long userId, Long conversationId) {
        AiConversation conversation = requireConversation(conversationId, userId);
        conversation.setStatus(DELETED);
        conversationRepository.save(conversation);
        log.info("ai conversation deleted userId={} conversationId={}", userId, conversationId);
    }

    private List<CitationVO> retrieve(String question) {
        Map<Long, RetrievalCandidate> candidates = new LinkedHashMap<>();
        for (String query : expandQueries(question)) {
            for (String keyword : keywords(query)) {
                List<VideoKnowledge> videos = videoRepository.searchOnline(
                        "%" + keyword + "%", PageRequest.of(0, Math.max(ragCandidateLimit, ragTopK)));
                for (VideoKnowledge video : videos) {
                    candidates.computeIfAbsent(video.getId(), ignored -> new RetrievalCandidate(video))
                            .add(keyword, score(video, question, keyword));
                }
            }
        }
        if (candidates.isEmpty()) {
            for (VideoKnowledge video : videoRepository.findTop8ByStatusOrderByPlayCountDescLikeCountDescCollectCountDesc(ONLINE)) {
                candidates.computeIfAbsent(video.getId(), ignored -> new RetrievalCandidate(video))
                        .add("热门课程", popularityScore(video));
                if (candidates.size() >= Math.min(3, ragTopK)) {
                    break;
                }
            }
        }
        List<CitationVO> citations = candidates.values().stream()
                .sorted(Comparator.comparingDouble(RetrievalCandidate::getScore).reversed())
                .limit(ragTopK)
                .map(this::toCitation)
                .collect(Collectors.toList());
        log.info("ai rag retrieved questionLength={} candidates={} returned={}",
                question.length(), candidates.size(), citations.size());
        return citations;
    }

    private String generateAnswer(String question, List<CitationVO> citations) {
        String systemPrompt = "你是杏林学堂的中医学习助手。回答必须面向学习场景，表达谨慎，不做诊断，不替代医生。优先引用课程资料，并在涉及症状、用药、治疗判断时提示咨询执业医师。";
        String userPrompt = buildPrompt(question, citations);
        String llmAnswer = llmClient.answer(systemPrompt, userPrompt);
        if (StringUtils.hasText(llmAnswer)) {
            return llmAnswer.trim();
        }
        return localAnswer(question, citations);
    }

    private String buildPrompt(String question, List<CitationVO> citations) {
        StringBuilder builder = new StringBuilder();
        builder.append("用户问题：").append(question).append("\n\n");
        builder.append("可引用课程：\n");
        if (citations.isEmpty()) {
            builder.append("暂无匹配课程。\n");
        } else {
            for (int i = 0; i < citations.size(); i++) {
                CitationVO item = citations.get(i);
                builder.append(i + 1).append(". ").append(item.getTitle())
                        .append("，讲师：").append(nullToDefault(item.getLecturer(), "未填写"))
                        .append("，匹配分：").append(item.getRelevanceScore())
                        .append("，命中词：").append(item.getMatchedKeywords())
                        .append("，摘要：").append(nullToDefault(item.getSummary(), "暂无摘要")).append("\n");
            }
        }
        builder.append("\n请给出结构化回答，包含：核心解释、学习建议、推荐课程引用、注意事项。");
        return builder.toString();
    }

    private String localAnswer(String question, List<CitationVO> citations) {
        StringBuilder builder = new StringBuilder();
        builder.append("我已收到你的问题：“").append(question).append("”。\n\n");
        builder.append("建议先按“概念理解 -> 典型例子 -> 课程巩固 -> 自我复盘”的顺序学习。");
        builder.append("如果问题涉及具体症状、用药或治疗判断，请以执业医师意见为准。\n\n");
        if (citations.isEmpty()) {
            builder.append("当前课程库里没有检索到强相关内容。你可以换一个更具体的关键词，例如病机、方剂、经络、阴阳五行、针灸取穴等。");
        } else {
            builder.append("根据课程库，建议优先查看：\n");
            for (int i = 0; i < citations.size(); i++) {
                CitationVO item = citations.get(i);
                builder.append(i + 1).append(". ").append(item.getTitle());
                if (StringUtils.hasText(item.getLecturer())) {
                    builder.append("（").append(item.getLecturer()).append("）");
                }
                if (!item.getMatchedKeywords().isEmpty()) {
                    builder.append("，命中：").append(item.getMatchedKeywords());
                }
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private List<String> expandQueries(String question) {
        List<String> queries = new ArrayList<>();
        queries.add(question);
        String normalized = question.toLowerCase();
        if (containsAny(normalized, "入门", "基础", "怎么学", "如何学", "学习计划", "初学")) {
            queries.add(question + " 中医基础 阴阳五行 藏象 经络 方剂 学习路径");
        }
        if (containsAny(normalized, "阴阳", "五行")) {
            queries.add(question + " 阴阳五行 生克制化 藏象 病机");
        }
        if (containsAny(normalized, "方剂", "中药", "药性", "本草")) {
            queries.add(question + " 方剂组成 功效 主治 配伍 禁忌 中药药性");
        }
        if (containsAny(normalized, "经络", "穴位", "腧穴", "针灸", "艾灸")) {
            queries.add(question + " 经络腧穴 针灸 取穴 主治");
        }
        String hyde = llmClient.answer(
                "你是中医课程检索助手，只输出一段不超过120字的课程检索描述，不要回答问题。",
                "为这个问题生成适合检索课程库的假想课程摘要：" + question);
        if (StringUtils.hasText(hyde)) {
            queries.add(summary(hyde, 120));
        }
        return queries.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .limit(6)
                .collect(Collectors.toList());
    }

    private List<String> keywords(String question) {
        Set<String> values = new LinkedHashSet<>();
        String clean = question.replaceAll("[,.;:!?\\[\\](){}<>\"'`~@#$%^&*+=|\\\\/\\r\\n\\t，。！？、；：（）【】《》“”‘’]", " ").trim();
        for (String item : clean.split("\\s+")) {
            String value = item.trim();
            if (value.length() >= 2) {
                values.add(value.length() > 30 ? value.substring(0, 30) : value);
                addChineseNgrams(values, value);
            }
        }
        String normalized = question.toLowerCase();
        for (String term : DOMAIN_TERMS) {
            if (normalized.contains(term.toLowerCase())) {
                values.add(term);
            }
        }
        if (values.isEmpty() && clean.length() >= 2) {
            values.add(clean.length() > 30 ? clean.substring(0, 30) : clean);
        }
        if (values.isEmpty()) {
            values.add(question);
        }
        return values.stream().limit(16).collect(Collectors.toList());
    }

    private void addChineseNgrams(Set<String> values, String value) {
        if (value.length() < 4 || !value.matches(".*[\\u4e00-\\u9fa5].*")) {
            return;
        }
        int limit = Math.min(value.length(), 12);
        for (int size = 2; size <= 4; size++) {
            for (int index = 0; index + size <= limit && values.size() < 20; index++) {
                values.add(value.substring(index, index + size));
            }
        }
    }

    private double score(VideoKnowledge video, String question, String keyword) {
        String title = lower(video.getTitle());
        String lecturer = lower(video.getLecturer());
        String tags = lower(video.getTags());
        String description = lower(video.getDescription());
        String key = lower(keyword);
        double score = popularityScore(video);
        if (StringUtils.hasText(key)) {
            if (title.contains(key)) {
                score += 12D;
            }
            if (tags.contains(key)) {
                score += 8D;
            }
            if (lecturer.contains(key)) {
                score += 5D;
            }
            if (description.contains(key)) {
                score += 3D;
            }
        }
        for (String term : keywords(question)) {
            String value = lower(term);
            if (title.contains(value)) {
                score += 3D;
            } else if (tags.contains(value)) {
                score += 2D;
            } else if (description.contains(value)) {
                score += 1D;
            }
        }
        return score;
    }

    private double popularityScore(VideoKnowledge video) {
        long play = video.getPlayCount() == null ? 0L : video.getPlayCount();
        long like = video.getLikeCount() == null ? 0L : video.getLikeCount();
        long collect = video.getCollectCount() == null ? 0L : video.getCollectCount();
        return Math.log10(play + like * 2D + collect * 3D + 1D);
    }

    private boolean containsAny(String value, String... terms) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        for (String term : terms) {
            if (value.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private void checkRateLimit(Long userId) {
        String minute = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String key = "ai:rate:" + userId + ":" + minute;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofMinutes(2));
        }
        if (count != null && count > rateLimitPerMinute) {
            throw new BusinessException(429, "提问过于频繁，请稍后再试");
        }
    }

    private AiConversation createConversationEntity(Long userId, String title) {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setTitle(title);
        return conversationRepository.save(conversation);
    }

    private AiConversation requireConversation(Long conversationId, Long userId) {
        if (conversationId == null || conversationId <= 0) {
            throw new BusinessException(400, "会话ID不合法");
        }
        return conversationRepository.findByIdAndUserIdAndStatus(conversationId, userId, ACTIVE)
                .orElseThrow(() -> new BusinessException(404, "会话不存在"));
    }

    private AiMessage saveMessage(Long conversationId, Long userId, String role, String content, List<CitationVO> citations) {
        AiMessage message = new AiMessage();
        message.setConversationId(conversationId);
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        if (citations != null && !citations.isEmpty()) {
            try {
                message.setCitationsJson(objectMapper.writeValueAsString(citations));
            } catch (Exception ex) {
                log.warn("serialize citations failed conversationId={} error={}", conversationId, ex.getMessage());
            }
        }
        return messageRepository.save(message);
    }

    private AiConversationVO toConversation(AiConversation conversation) {
        AiConversationVO vo = new AiConversationVO();
        vo.setId(conversation.getId());
        vo.setTitle(conversation.getTitle());
        vo.setLastQuestion(conversation.getLastQuestion());
        vo.setLastAnswerPreview(conversation.getLastAnswerPreview());
        vo.setMessageCount(nonNull(conversation.getMessageCount()));
        vo.setCreatedAt(conversation.getCreatedAt());
        vo.setUpdatedAt(conversation.getUpdatedAt());
        return vo;
    }

    private AiMessageVO toMessage(AiMessage message) {
        AiMessageVO vo = new AiMessageVO();
        vo.setId(message.getId());
        vo.setConversationId(message.getConversationId());
        vo.setRole(message.getRole());
        vo.setContent(message.getContent());
        vo.setCreatedAt(message.getCreatedAt());
        if (StringUtils.hasText(message.getCitationsJson())) {
            try {
                vo.setCitations(objectMapper.readValue(message.getCitationsJson(), new TypeReference<List<CitationVO>>() {}));
            } catch (Exception ex) {
                log.warn("parse citations failed messageId={} error={}", message.getId(), ex.getMessage());
            }
        }
        return vo;
    }

    private CitationVO toCitation(VideoKnowledge video) {
        CitationVO vo = new CitationVO();
        vo.setVideoId(video.getId());
        vo.setTitle(video.getTitle());
        vo.setLecturer(video.getLecturer());
        vo.setSummary(summary(video.getDescription(), 160));
        vo.setVideoUrl(video.getVideoUrl());
        vo.setCoverUrl(video.getCoverUrl());
        vo.setPlayCount(video.getPlayCount() == null ? 0L : video.getPlayCount());
        return vo;
    }

    private CitationVO toCitation(RetrievalCandidate candidate) {
        CitationVO vo = toCitation(candidate.getVideo());
        vo.setRelevanceScore(Math.round(candidate.getScore() * 100D) / 100D);
        vo.setMatchedKeywords(new ArrayList<>(candidate.getMatchedKeywords()));
        return vo;
    }

    private String titleFromQuestion(String question) {
        return summary(question, 32);
    }

    private String cleanText(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, "内容不能为空");
        }
        String clean = value.trim();
        return clean.length() > maxLength ? clean.substring(0, maxLength) : clean;
    }

    private String summary(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String clean = value.trim().replaceAll("\\s+", " ");
        return clean.length() <= maxLength ? clean : clean.substring(0, maxLength) + "...";
    }

    private String nullToDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private String lower(String value) {
        return StringUtils.hasText(value) ? value.toLowerCase() : "";
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

    private int nonNull(Integer value) {
        return value == null ? 0 : value;
    }

    private static class RetrievalCandidate {
        private final VideoKnowledge video;
        private final Set<String> matchedKeywords = new HashSet<>();
        private double score;

        RetrievalCandidate(VideoKnowledge video) {
            this.video = video;
        }

        void add(String keyword, double value) {
            if (StringUtils.hasText(keyword)) {
                matchedKeywords.add(keyword);
            }
            score += value;
        }

        VideoKnowledge getVideo() {
            return video;
        }

        Set<String> getMatchedKeywords() {
            return matchedKeywords;
        }

        double getScore() {
            return score;
        }
    }
}
