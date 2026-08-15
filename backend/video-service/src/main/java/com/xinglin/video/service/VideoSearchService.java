package com.xinglin.video.service;

import com.xinglin.video.common.PageResponse;
import com.xinglin.video.dto.VideoQueryRequest;
import com.xinglin.video.entity.LearningRecord;
import com.xinglin.video.entity.Video;
import com.xinglin.video.repository.LearningRecordRepository;
import com.xinglin.video.repository.VideoRepository;
import com.xinglin.video.search.VideoSearchDocument;
import com.xinglin.video.search.VideoSearchRepository;
import com.xinglin.video.vo.CategoryVO;
import com.xinglin.video.vo.VideoCardVO;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VideoSearchService {
    private static final Logger log = LoggerFactory.getLogger(VideoSearchService.class);
    private static final String ONLINE = "ONLINE";

    private final VideoSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final LearningRecordRepository learningRecordRepository;
    private final VideoRepository videoRepository;
    private final VideoCategoryService categoryService;

    @Value("${xinglin.elasticsearch.enabled:true}")
    private boolean enabled;

    public VideoSearchService(VideoSearchRepository searchRepository,
                              ElasticsearchOperations elasticsearchOperations,
                              LearningRecordRepository learningRecordRepository,
                              VideoRepository videoRepository,
                              VideoCategoryService categoryService) {
        this.searchRepository = searchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.learningRecordRepository = learningRecordRepository;
        this.videoRepository = videoRepository;
        this.categoryService = categoryService;
    }

    public Optional<PageResponse<VideoCardVO>> search(VideoQueryRequest request, Long userId, int page, int pageSize) {
        if (!enabled || !StringUtils.hasText(request.getKeyword())) {
            return Optional.empty();
        }
        try {
            BoolQueryBuilder query = QueryBuilders.boolQuery()
                    .must(QueryBuilders.multiMatchQuery(request.getKeyword().trim(), "title", "description", "lecturer", "tags")
                            .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                            .field("title", 4F)
                            .field("tags", 3F)
                            .field("lecturer", 2F)
                            .field("description", 1F))
                    .filter(QueryBuilders.termQuery("status", ONLINE));
            if (request.getCategoryId() != null) {
                query.filter(QueryBuilders.termQuery("categoryId", request.getCategoryId()));
            }
            if (StringUtils.hasText(request.getTag())) {
                query.filter(QueryBuilders.matchQuery("tags", request.getTag().trim()));
            }
            if (request.getMinDuration() != null || request.getMaxDuration() != null) {
                org.elasticsearch.index.query.RangeQueryBuilder range = QueryBuilders.rangeQuery("duration");
                if (request.getMinDuration() != null) {
                    range.gte(request.getMinDuration());
                }
                if (request.getMaxDuration() != null) {
                    range.lte(request.getMaxDuration());
                }
                query.filter(range);
            }
            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                    .withQuery(query)
                    .withPageable(PageRequest.of(page - 1, pageSize));
            applySort(builder, request.getSort());
            SearchHits<VideoSearchDocument> hits = elasticsearchOperations.search(builder.build(), VideoSearchDocument.class);
            List<VideoCardVO> records = hits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(document -> toCard(document, userId))
                    .collect(Collectors.toList());
            log.info("video es search success keyword={} page={} pageSize={} total={}",
                    request.getKeyword(), page, pageSize, hits.getTotalHits());
            return Optional.of(new PageResponse<>(records, page, pageSize, hits.getTotalHits()));
        } catch (Exception ex) {
            log.warn("video es search fallback keyword={} error={}", request.getKeyword(), ex.getMessage());
            return Optional.empty();
        }
    }

    public void sync(Video video) {
        if (!enabled || video == null || video.getId() == null) {
            return;
        }
        try {
            if (ONLINE.equals(video.getStatus())) {
                searchRepository.save(toDocument(video));
            } else {
                searchRepository.deleteById(video.getId());
            }
            log.info("video es sync success videoId={} status={}", video.getId(), video.getStatus());
        } catch (Exception ex) {
            log.warn("video es sync failed videoId={} error={}", video.getId(), ex.getMessage());
        }
    }

    public void delete(Long videoId) {
        if (!enabled || videoId == null) {
            return;
        }
        try {
            searchRepository.deleteById(videoId);
            log.info("video es delete success videoId={}", videoId);
        } catch (Exception ex) {
            log.warn("video es delete failed videoId={} error={}", videoId, ex.getMessage());
        }
    }

    public long rebuild() {
        if (!enabled) {
            return 0L;
        }
        Iterable<VideoSearchDocument> documents = videoRepository.findAll().stream()
                .filter(video -> ONLINE.equals(video.getStatus()))
                .map(this::toDocument)
                .collect(Collectors.toList());
        searchRepository.deleteAll();
        Iterable<VideoSearchDocument> saved = searchRepository.saveAll(documents);
        long count = 0L;
        for (VideoSearchDocument ignored : saved) {
            count++;
        }
        log.info("video es rebuild success count={}", count);
        return count;
    }

    private void applySort(NativeSearchQueryBuilder builder, String sort) {
        if ("latest".equals(sort)) {
            builder.withSort(SortBuilders.fieldSort("publishTime").order(SortOrder.DESC));
            return;
        }
        if ("mostLiked".equals(sort)) {
            builder.withSort(SortBuilders.fieldSort("likeCount").order(SortOrder.DESC));
            return;
        }
        if ("mostCollected".equals(sort)) {
            builder.withSort(SortBuilders.fieldSort("collectCount").order(SortOrder.DESC));
            return;
        }
        if ("durationAsc".equals(sort)) {
            builder.withSort(SortBuilders.fieldSort("duration").order(SortOrder.ASC));
            return;
        }
        if ("durationDesc".equals(sort)) {
            builder.withSort(SortBuilders.fieldSort("duration").order(SortOrder.DESC));
            return;
        }
        builder.withSort(SortBuilders.scoreSort().order(SortOrder.DESC));
        builder.withSort(SortBuilders.fieldSort("playCount").order(SortOrder.DESC));
    }

    private VideoSearchDocument toDocument(Video video) {
        VideoSearchDocument document = new VideoSearchDocument();
        document.setId(video.getId());
        document.setTitle(video.getTitle());
        document.setDescription(video.getDescription());
        document.setCategoryId(video.getCategoryId());
        document.setCategoryName(resolveCategoryName(video.getCategoryId()));
        document.setLecturer(video.getLecturer());
        document.setTags(video.getTags());
        document.setStatus(video.getStatus());
        document.setCoverUrl(video.getCoverUrl());
        document.setVideoUrl(video.getVideoUrl());
        document.setDuration(video.getDuration());
        document.setPlayCount(nonNull(video.getPlayCount()));
        document.setLikeCount(nonNull(video.getLikeCount()));
        document.setCollectCount(nonNull(video.getCollectCount()));
        document.setPublishTime(video.getPublishTime());
        return document;
    }

    private VideoCardVO toCard(VideoSearchDocument document, Long userId) {
        VideoCardVO vo = new VideoCardVO();
        vo.setId(document.getId());
        vo.setTitle(document.getTitle());
        vo.setCoverUrl(document.getCoverUrl());
        vo.setLecturer(document.getLecturer());
        vo.setCategoryId(document.getCategoryId());
        vo.setCategoryName(document.getCategoryName());
        vo.setTags(splitTags(document.getTags()));
        vo.setDuration(document.getDuration());
        vo.setPlayCount(nonNull(document.getPlayCount()));
        vo.setLikeCount(nonNull(document.getLikeCount()));
        vo.setCollectCount(nonNull(document.getCollectCount()));
        vo.setStatus(document.getStatus());
        vo.setPublishTime(document.getPublishTime());
        if (userId != null) {
            learningRecordRepository.findByUserIdAndVideoId(userId, document.getId()).ifPresent(record -> enrichProgress(vo, record));
        }
        return vo;
    }

    private void enrichProgress(VideoCardVO vo, LearningRecord record) {
        vo.setProgressPercent(record.getProgressPercent());
        vo.setFinished(record.getFinished());
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

    private long nonNull(Long value) {
        return value == null ? 0L : value;
    }

    private String resolveCategoryName(Long categoryId) {
        if (categoryId == null) {
            return "未分类";
        }
        return categoryService.listAllCategories().stream()
                .filter(category -> categoryId.equals(category.getId()))
                .map(CategoryVO::getName)
                .findFirst()
                .orElse("未分类");
    }
}
