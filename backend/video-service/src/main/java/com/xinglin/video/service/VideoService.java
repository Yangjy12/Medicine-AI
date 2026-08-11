package com.xinglin.video.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.xinglin.video.common.BusinessException;
import com.xinglin.video.common.PageResponse;
import com.xinglin.video.config.RabbitConfig;
import com.xinglin.video.dto.PlayRequest;
import com.xinglin.video.dto.ProgressRequest;
import com.xinglin.video.dto.SaveVideoRequest;
import com.xinglin.video.dto.VideoQueryRequest;
import com.xinglin.video.entity.*;
import com.xinglin.video.repository.*;
import com.xinglin.video.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoService {
    private static final Logger log = LoggerFactory.getLogger(VideoService.class);
    private static final String ONLINE = "ONLINE";

    private final VideoRepository videoRepository;
    private final VideoCategoryRepository categoryRepository;
    private final LearningRecordRepository learningRecordRepository;
    private final VideoFavoriteRepository favoriteRepository;
    private final VideoLikeRepository likeRepository;
    private final VideoCategoryService categoryService;
    private final Cache<Long, VideoDetailVO> videoDetailLocalCache;
    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Value("${xinglin.video.page-size-max:50}")
    private int pageSizeMax;

    @Value("${xinglin.video.finish-threshold-percent:90}")
    private int finishThresholdPercent;

    @Value("${xinglin.video.effective-play-seconds:10}")
    private int effectivePlaySeconds;

    @Value("${xinglin.video.play-dedup-minutes:30}")
    private int playDedupMinutes;

    public VideoService(VideoRepository videoRepository,
                        VideoCategoryRepository categoryRepository,
                        LearningRecordRepository learningRecordRepository,
                        VideoFavoriteRepository favoriteRepository,
                        VideoLikeRepository likeRepository,
                        VideoCategoryService categoryService,
                        Cache<Long, VideoDetailVO> videoDetailLocalCache,
                        StringRedisTemplate redisTemplate,
                        RabbitTemplate rabbitTemplate) {
        this.videoRepository = videoRepository;
        this.categoryRepository = categoryRepository;
        this.learningRecordRepository = learningRecordRepository;
        this.favoriteRepository = favoriteRepository;
        this.likeRepository = likeRepository;
        this.categoryService = categoryService;
        this.videoDetailLocalCache = videoDetailLocalCache;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    public HomeVO home(Long userId) {
        log.info("video home query userId={}", userId);
        HomeVO home = new HomeVO();
        home.setCategories(categoryService.listEnabledCategories());
        home.setLatest(videoRepository.findTop8ByStatusOrderByPublishTimeDesc(ONLINE).stream().map(v -> toCard(v, userId)).collect(Collectors.toList()));
        home.setHot(videoRepository.findTop10ByStatusOrderByPlayCountDescLikeCountDescCollectCountDesc(ONLINE).stream().map(v -> toCard(v, userId)).collect(Collectors.toList()));
        home.setRecommended(home.getHot().stream().limit(6).collect(Collectors.toList()));
        if (userId != null) {
            VideoQueryRequest request = new VideoQueryRequest();
            request.setPage(1);
            request.setPageSize(6);
            home.setContinueLearning(learningHistory(userId, request).getRecords());
        }
        return home;
    }

    public PageResponse<VideoCardVO> query(VideoQueryRequest request, Long userId) {
        log.info("video query keyword={} categoryId={} sort={} page={} pageSize={} userId={}",
                request.getKeyword(), request.getCategoryId(), request.getSort(), request.getPage(), request.getPageSize(), userId);
        int page = normalizePage(request.getPage());
        int pageSize = normalizePageSize(request.getPageSize());
        Pageable pageable = PageRequest.of(page - 1, pageSize, buildSort(request.getSort()));
        Page<Video> result = videoRepository.findAll(buildSpecification(request, true), pageable);
        List<VideoCardVO> records = result.getContent().stream()
                .map(video -> toCard(video, userId))
                .collect(Collectors.toList());
        recordSearchWord(request.getKeyword());
        return new PageResponse<>(records, page, pageSize, result.getTotalElements());
    }

    public PageResponse<VideoCardVO> adminQuery(VideoQueryRequest request, Long adminId) {
        log.info("admin video query keyword={} categoryId={} sort={} page={} pageSize={} adminId={}",
                request.getKeyword(), request.getCategoryId(), request.getSort(), request.getPage(), request.getPageSize(), adminId);
        int page = normalizePage(request.getPage());
        int pageSize = normalizePageSize(request.getPageSize());
        Pageable pageable = PageRequest.of(page - 1, pageSize, buildSort(request.getSort()));
        Page<Video> result = videoRepository.findAll(buildSpecification(request, false), pageable);
        List<VideoCardVO> records = result.getContent().stream()
                .map(video -> toCard(video, null))
                .collect(Collectors.toList());
        return new PageResponse<>(records, page, pageSize, result.getTotalElements());
    }

    public VideoDetailVO adminDetail(Long videoId, Long adminId) {
        log.info("admin video detail query videoId={} adminId={}", videoId, adminId);
        if (videoId == null || videoId <= 0) {
            throw new BusinessException(400, "视频ID不合法");
        }
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new BusinessException(404, "视频不存在"));
        return toDetail(video);
    }

    public VideoDetailVO detail(Long videoId, Long userId) {
        log.info("video detail query videoId={} userId={}", videoId, userId);
        if (videoId == null || videoId <= 0) {
            throw new BusinessException(400, "视频ID不合法");
        }

        VideoDetailVO base = videoDetailLocalCache.getIfPresent(videoId);
        if (base == null) {
            log.info("video detail local cache miss videoId={}", videoId);
            Video video = videoRepository.findById(videoId)
                    .orElseThrow(() -> new BusinessException(404, "视频不存在"));
            base = toDetail(video);
            videoDetailLocalCache.put(videoId, base);
        } else {
            log.info("video detail local cache hit videoId={}", videoId);
        }

        VideoDetailVO result = cloneDetail(base);
        if (!ONLINE.equals(result.getStatus())) {
            throw new BusinessException(404, "视频已下架");
        }
        enrichUserState(result, userId);
        result.setRelatedVideos(related(videoId, 6, userId));
        return result;
    }

    public List<VideoCardVO> related(Long videoId, int limit, Long userId) {
        Video current = videoRepository.findById(videoId)
                .orElseThrow(() -> new BusinessException(404, "视频不存在"));
        Pageable pageable = PageRequest.of(0, Math.max(1, Math.min(limit, 12)));
        return videoRepository.findByCategoryIdAndStatusAndIdNotOrderByPlayCountDesc(current.getCategoryId(), ONLINE, videoId, pageable)
                .stream()
                .map(video -> toCard(video, userId))
                .collect(Collectors.toList());
    }

    public void recordPlay(Long videoId, Long userId, String ip, PlayRequest request) {
        log.info("video play report videoId={} userId={} ip={} playedSecond={}", videoId, userId, ip, request.getPlayedSecond());
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new BusinessException(404, "视频不存在"));
        int played = Optional.ofNullable(request.getPlayedSecond()).orElse(0);
        if (played < effectivePlaySeconds) {
            log.info("video play ignored by threshold videoId={} userId={} playedSecond={}", videoId, userId, played);
            return;
        }
        String identity = userId != null ? String.valueOf(userId) : Integer.toHexString(Objects.toString(ip, "anonymous").hashCode());
        String dedupKey = "video:play:dedup:" + videoId + ":" + identity;
        Boolean inserted = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1", Duration.ofMinutes(playDedupMinutes));
        if (Boolean.FALSE.equals(inserted)) {
            log.info("video play ignored by dedup videoId={} identity={}", videoId, identity);
            return;
        }
        video.setPlayCount(Optional.ofNullable(video.getPlayCount()).orElse(0L) + 1);
        videoRepository.save(video);
        videoDetailLocalCache.invalidate(videoId);
        redisTemplate.opsForZSet().incrementScore("video:rank:hot:total", String.valueOf(videoId), 1);
        log.info("video play counted videoId={} userId={} playCount={}", videoId, userId, video.getPlayCount());
    }

    @Transactional
    public ProgressVO updateProgress(Long videoId, Long userId, ProgressRequest request) {
        log.info("video progress update videoId={} userId={} currentSecond={} duration={}",
                videoId, userId, request.getCurrentSecond(), request.getDuration());
        requireLogin(userId);
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new BusinessException(404, "视频不存在"));
        int duration = Math.max(1, Optional.ofNullable(request.getDuration()).orElse(video.getDuration()));
        int current = Math.max(0, Math.min(request.getCurrentSecond(), duration));
        int percent = (int) Math.floor(current * 100.0 / duration);
        boolean finished = percent >= finishThresholdPercent;

        LearningRecord record = learningRecordRepository.findByUserIdAndVideoId(userId, videoId)
                .orElseGet(() -> {
                    LearningRecord created = new LearningRecord();
                    created.setUserId(userId);
                    created.setVideoId(videoId);
                    return created;
                });
        boolean firstFinish = finished && !Boolean.TRUE.equals(record.getFinished());
        record.setCurrentSecond(current);
        record.setDuration(duration);
        record.setProgressPercent(percent);
        record.setFinished(finished);
        record.setLastLearnTime(LocalDateTime.now());
        if (firstFinish) {
            record.setFirstFinishTime(LocalDateTime.now());
        }
        learningRecordRepository.save(record);

        if (firstFinish) {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("eventId", UUID.randomUUID().toString());
            event.put("eventType", "VIDEO_LEARNING_FINISHED");
            event.put("userId", userId);
            event.put("videoId", videoId);
            event.put("finishedAt", LocalDateTime.now().toString());
            rabbitTemplate.convertAndSend(RabbitConfig.VIDEO_EXCHANGE, RabbitConfig.LEARNING_ROUTING_KEY, event);
            log.info("video learning finished event sent userId={} videoId={} eventId={}", userId, videoId, event.get("eventId"));
        }
        return new ProgressVO(current, percent, finished);
    }

    @Transactional
    public void like(Long videoId, Long userId) {
        log.info("video like request videoId={} userId={}", videoId, userId);
        requireLogin(userId);
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new BusinessException(404, "视频不存在"));
        if (!likeRepository.existsByUserIdAndVideoId(userId, videoId)) {
            likeRepository.save(new VideoLike(userId, videoId));
            video.setLikeCount(Optional.ofNullable(video.getLikeCount()).orElse(0L) + 1);
            videoRepository.save(video);
            videoDetailLocalCache.invalidate(videoId);
            log.info("video like success videoId={} userId={} likeCount={}", videoId, userId, video.getLikeCount());
        }
    }

    @Transactional
    public void unlike(Long videoId, Long userId) {
        log.info("video unlike request videoId={} userId={}", videoId, userId);
        requireLogin(userId);
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new BusinessException(404, "视频不存在"));
        if (likeRepository.existsByUserIdAndVideoId(userId, videoId)) {
            likeRepository.deleteByUserIdAndVideoId(userId, videoId);
            video.setLikeCount(Math.max(0L, Optional.ofNullable(video.getLikeCount()).orElse(0L) - 1));
            videoRepository.save(video);
            videoDetailLocalCache.invalidate(videoId);
            log.info("video unlike success videoId={} userId={} likeCount={}", videoId, userId, video.getLikeCount());
        }
    }

    @Transactional
    public void favorite(Long videoId, Long userId) {
        log.info("video favorite request videoId={} userId={}", videoId, userId);
        requireLogin(userId);
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new BusinessException(404, "视频不存在"));
        if (!favoriteRepository.existsByUserIdAndVideoId(userId, videoId)) {
            favoriteRepository.save(new VideoFavorite(userId, videoId));
            video.setCollectCount(Optional.ofNullable(video.getCollectCount()).orElse(0L) + 1);
            videoRepository.save(video);
            videoDetailLocalCache.invalidate(videoId);
            log.info("video favorite success videoId={} userId={} collectCount={}", videoId, userId, video.getCollectCount());
        }
    }

    @Transactional
    public void unfavorite(Long videoId, Long userId) {
        log.info("video unfavorite request videoId={} userId={}", videoId, userId);
        requireLogin(userId);
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new BusinessException(404, "视频不存在"));
        if (favoriteRepository.existsByUserIdAndVideoId(userId, videoId)) {
            favoriteRepository.deleteByUserIdAndVideoId(userId, videoId);
            video.setCollectCount(Math.max(0L, Optional.ofNullable(video.getCollectCount()).orElse(0L) - 1));
            videoRepository.save(video);
            videoDetailLocalCache.invalidate(videoId);
            log.info("video unfavorite success videoId={} userId={} collectCount={}", videoId, userId, video.getCollectCount());
        }
    }

    public PageResponse<VideoCardVO> learningHistory(Long userId, VideoQueryRequest request) {
        log.info("video learning history query userId={} page={} pageSize={}", userId, request.getPage(), request.getPageSize());
        requireLogin(userId);
        int page = normalizePage(request.getPage());
        int pageSize = normalizePageSize(request.getPageSize());
        Page<LearningRecord> records = learningRecordRepository.findByUserIdOrderByLastLearnTimeDesc(userId, PageRequest.of(page - 1, pageSize));
        List<VideoCardVO> videos = records.getContent().stream()
                .map(record -> videoRepository.findById(record.getVideoId()).map(video -> toCard(video, userId)).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new PageResponse<>(videos, page, pageSize, records.getTotalElements());
    }

    public PageResponse<VideoCardVO> favorites(Long userId, VideoQueryRequest request) {
        log.info("video favorites query userId={} page={} pageSize={}", userId, request.getPage(), request.getPageSize());
        requireLogin(userId);
        int page = normalizePage(request.getPage());
        int pageSize = normalizePageSize(request.getPageSize());
        Page<VideoFavorite> records = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page - 1, pageSize));
        List<VideoCardVO> videos = records.getContent().stream()
                .map(record -> videoRepository.findById(record.getVideoId()).map(video -> toCard(video, userId)).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new PageResponse<>(videos, page, pageSize, records.getTotalElements());
    }

    @Transactional
    public VideoDetailVO saveVideo(SaveVideoRequest request, Long adminId) {
        log.info("admin save video id={} title={} status={} adminId={}", request.getId(), request.getTitle(), request.getStatus(), adminId);
        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw new BusinessException(404, "视频分类不存在");
        }
        String status = StringUtils.hasText(request.getStatus()) ? request.getStatus().trim().toUpperCase(Locale.ROOT) : "DRAFT";
        if (!Arrays.asList("DRAFT", "ONLINE", "OFFLINE").contains(status)) {
            throw new BusinessException(400, "视频状态不合法");
        }
        Video video = request.getId() == null
                ? new Video()
                : videoRepository.findById(request.getId()).orElseThrow(() -> new BusinessException(404, "视频不存在"));
        video.setTitle(request.getTitle().trim());
        video.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        video.setCategoryId(request.getCategoryId());
        video.setLecturer(request.getLecturer() == null ? null : request.getLecturer().trim());
        video.setCoverUrl(request.getCoverUrl().trim());
        video.setVideoUrl(request.getVideoUrl().trim());
        video.setDuration(request.getDuration());
        video.setTags(request.getTags() == null ? null : request.getTags().trim());
        video.setStatus(status);
        if (video.getCreatedBy() == null) {
            video.setCreatedBy(adminId);
        }
        if (ONLINE.equals(status) && video.getPublishTime() == null) {
            video.setPublishTime(LocalDateTime.now());
        }
        Video saved = videoRepository.save(video);
        videoDetailLocalCache.invalidate(saved.getId());
        return toDetail(saved);
    }

    @Transactional
    public void updateStatus(Long videoId, String status) {
        log.info("admin update video status videoId={} status={}", videoId, status);
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new BusinessException(404, "视频不存在"));
        video.setStatus(status);
        if (ONLINE.equals(status) && video.getPublishTime() == null) {
            video.setPublishTime(LocalDateTime.now());
        }
        videoRepository.save(video);
        videoDetailLocalCache.invalidate(videoId);
    }

    private Specification<Video> buildSpecification(VideoQueryRequest request, boolean onlyOnline) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (onlyOnline) {
                predicates.add(builder.equal(root.get("status"), ONLINE));
            }
            if (request.getCategoryId() != null) {
                predicates.add(builder.equal(root.get("categoryId"), request.getCategoryId()));
            }
            if (StringUtils.hasText(request.getKeyword())) {
                String keyword = "%" + request.getKeyword().trim() + "%";
                predicates.add(builder.or(
                        builder.like(root.get("title"), keyword),
                        builder.like(root.get("description"), keyword),
                        builder.like(root.get("lecturer"), keyword),
                        builder.like(root.get("tags"), keyword)
                ));
            }
            if (StringUtils.hasText(request.getTag())) {
                predicates.add(builder.like(root.get("tags"), "%" + request.getTag().trim() + "%"));
            }
            if (request.getMinDuration() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("duration"), request.getMinDuration()));
            }
            if (request.getMaxDuration() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("duration"), request.getMaxDuration()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort buildSort(String sort) {
        if ("latest".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "publishTime");
        }
        if ("mostLiked".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "likeCount");
        }
        if ("mostCollected".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "collectCount");
        }
        if ("durationAsc".equals(sort)) {
            return Sort.by(Sort.Direction.ASC, "duration");
        }
        if ("durationDesc".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "duration");
        }
        return Sort.by(Sort.Direction.DESC, "playCount", "likeCount", "collectCount", "publishTime");
    }

    private VideoCardVO toCard(Video video, Long userId) {
        VideoCardVO vo = new VideoCardVO();
        vo.setId(video.getId());
        vo.setTitle(video.getTitle());
        vo.setCoverUrl(video.getCoverUrl());
        vo.setLecturer(video.getLecturer());
        vo.setCategoryId(video.getCategoryId());
        vo.setCategoryName(resolveCategoryName(video.getCategoryId()));
        vo.setTags(splitTags(video.getTags()));
        vo.setDuration(video.getDuration());
        vo.setPlayCount(Optional.ofNullable(video.getPlayCount()).orElse(0L));
        vo.setLikeCount(Optional.ofNullable(video.getLikeCount()).orElse(0L));
        vo.setCollectCount(Optional.ofNullable(video.getCollectCount()).orElse(0L));
        vo.setStatus(video.getStatus());
        vo.setPublishTime(video.getPublishTime());
        if (userId != null) {
            learningRecordRepository.findByUserIdAndVideoId(userId, video.getId()).ifPresent(record -> {
                vo.setProgressPercent(record.getProgressPercent());
                vo.setFinished(record.getFinished());
            });
        }
        return vo;
    }

    private VideoDetailVO toDetail(Video video) {
        VideoDetailVO vo = new VideoDetailVO();
        VideoCardVO card = toCard(video, null);
        vo.setId(card.getId());
        vo.setTitle(card.getTitle());
        vo.setCoverUrl(card.getCoverUrl());
        vo.setLecturer(card.getLecturer());
        vo.setCategoryId(card.getCategoryId());
        vo.setCategoryName(card.getCategoryName());
        vo.setTags(card.getTags());
        vo.setDuration(card.getDuration());
        vo.setPlayCount(card.getPlayCount());
        vo.setLikeCount(card.getLikeCount());
        vo.setCollectCount(card.getCollectCount());
        vo.setPublishTime(card.getPublishTime());
        vo.setDescription(video.getDescription());
        vo.setVideoUrl(video.getVideoUrl());
        vo.setStatus(video.getStatus());
        vo.setCreatedAt(video.getCreatedAt());
        vo.setUpdatedAt(video.getUpdatedAt());
        return vo;
    }

    private VideoDetailVO cloneDetail(VideoDetailVO source) {
        VideoDetailVO vo = new VideoDetailVO();
        vo.setId(source.getId());
        vo.setTitle(source.getTitle());
        vo.setCoverUrl(source.getCoverUrl());
        vo.setLecturer(source.getLecturer());
        vo.setCategoryId(source.getCategoryId());
        vo.setCategoryName(source.getCategoryName());
        vo.setTags(new ArrayList<>(source.getTags()));
        vo.setDuration(source.getDuration());
        vo.setPlayCount(source.getPlayCount());
        vo.setLikeCount(source.getLikeCount());
        vo.setCollectCount(source.getCollectCount());
        vo.setPublishTime(source.getPublishTime());
        vo.setDescription(source.getDescription());
        vo.setVideoUrl(source.getVideoUrl());
        vo.setStatus(source.getStatus());
        vo.setCreatedAt(source.getCreatedAt());
        vo.setUpdatedAt(source.getUpdatedAt());
        return vo;
    }

    private void enrichUserState(VideoDetailVO vo, Long userId) {
        if (userId == null) {
            return;
        }
        vo.setLiked(likeRepository.existsByUserIdAndVideoId(userId, vo.getId()));
        vo.setCollected(favoriteRepository.existsByUserIdAndVideoId(userId, vo.getId()));
        learningRecordRepository.findByUserIdAndVideoId(userId, vo.getId())
                .ifPresent(record -> vo.setProgress(new ProgressVO(record.getCurrentSecond(), record.getProgressPercent(), record.getFinished())));
    }

    private String resolveCategoryName(Long categoryId) {
        if (categoryId == null) {
            return "未分类";
        }
        return categoryRepository.findById(categoryId).map(VideoCategory::getName).orElse("未分类");
    }

    private List<String> splitTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return new ArrayList<>();
        }
        return Arrays.stream(tags.split("[,，]"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    private void recordSearchWord(String keyword) {
        if (StringUtils.hasText(keyword)) {
            redisTemplate.opsForZSet().incrementScore("video:search:hot_words", keyword.trim(), 1);
        }
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 12;
        }
        return Math.min(pageSize, pageSizeMax);
    }

    private void requireLogin(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(401, "请先登录");
        }
    }
}
