package com.xinglin.forum.service;

import com.xinglin.forum.common.BusinessException;
import com.xinglin.forum.common.PageResponse;
import com.xinglin.forum.dto.PostQueryRequest;
import com.xinglin.forum.dto.SaveCommentRequest;
import com.xinglin.forum.dto.SavePostRequest;
import com.xinglin.forum.entity.ForumComment;
import com.xinglin.forum.entity.ForumFavorite;
import com.xinglin.forum.entity.ForumLike;
import com.xinglin.forum.entity.ForumPost;
import com.xinglin.forum.entity.AppUserSummary;
import com.xinglin.forum.repository.ForumBoardRepository;
import com.xinglin.forum.repository.ForumCommentRepository;
import com.xinglin.forum.repository.ForumFavoriteRepository;
import com.xinglin.forum.repository.ForumLikeRepository;
import com.xinglin.forum.repository.ForumPostRepository;
import com.xinglin.forum.vo.CommentVO;
import com.xinglin.forum.vo.PostCardVO;
import com.xinglin.forum.vo.PostDetailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ForumService {
    private static final Logger log = LoggerFactory.getLogger(ForumService.class);
    private static final String POST = "POST";
    private static final String COMMENT = "COMMENT";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String DELETED = "DELETED";
    private static final String NORMAL = "NORMAL";
    private static final String VIEW_BUFFER_KEY = "forum:post:view:buffer";

    private final ForumPostRepository postRepository;
    private final ForumBoardRepository boardRepository;
    private final ForumCommentRepository commentRepository;
    private final ForumLikeRepository likeRepository;
    private final ForumFavoriteRepository favoriteRepository;
    private final ForumBoardService boardService;
    private final StringRedisTemplate redisTemplate;
    private final UserDirectoryService userDirectoryService;

    @Value("${xinglin.forum.page-size-max:50}")
    private int pageSizeMax;
    @Value("${xinglin.forum.view-dedup-minutes:30}")
    private int viewDedupMinutes;

    public ForumService(ForumPostRepository postRepository,
                        ForumBoardRepository boardRepository,
                        ForumCommentRepository commentRepository,
                        ForumLikeRepository likeRepository,
                        ForumFavoriteRepository favoriteRepository,
                        ForumBoardService boardService,
                        StringRedisTemplate redisTemplate,
                        UserDirectoryService userDirectoryService) {
        this.postRepository = postRepository;
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
        this.boardService = boardService;
        this.redisTemplate = redisTemplate;
        this.userDirectoryService = userDirectoryService;
    }

    public PageResponse<PostCardVO> queryPosts(PostQueryRequest request, Long userId, boolean admin) {
        int page = normalizePage(request.getPage());
        int pageSize = normalizePageSize(request.getPageSize());
        Pageable pageable = PageRequest.of(page - 1, pageSize, buildSort(request.getSort()));
        Page<ForumPost> result = postRepository.findAll(buildSpecification(request, admin), pageable);
        List<ForumPost> posts = result.getContent();
        Map<Long, AppUserSummary> users = usersForPosts(posts);
        List<PostCardVO> records = posts.stream()
                .map(post -> toCard(post, users))
                .collect(Collectors.toList());
        log.info("forum post query keyword={} boardId={} sort={} page={} pageSize={} userId={} admin={}",
                request.getKeyword(), request.getBoardId(), request.getSort(), page, pageSize, userId, admin);
        return new PageResponse<>(records, page, pageSize, result.getTotalElements());
    }

    public PageResponse<PostCardVO> myPosts(PostQueryRequest request, Long userId) {
        int page = normalizePage(request.getPage());
        int pageSize = normalizePageSize(request.getPageSize());
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ForumPost> result = postRepository.findByUserIdAndStatusNotOrderByCreatedAtDesc(userId, DELETED, pageable);
        List<ForumPost> posts = result.getContent();
        Map<Long, AppUserSummary> users = usersForPosts(posts);
        List<PostCardVO> records = posts.stream().map(post -> toCard(post, users)).collect(Collectors.toList());
        return new PageResponse<>(records, page, pageSize, result.getTotalElements());
    }

    public PageResponse<PostCardVO> myFavorites(PostQueryRequest request, Long userId) {
        int page = normalizePage(request.getPage());
        int pageSize = normalizePageSize(request.getPageSize());
        Page<ForumFavorite> result = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page - 1, pageSize));
        List<ForumPost> posts = result.getContent().stream()
                .map(favorite -> postRepository.findById(favorite.getPostId()).orElse(null))
                .filter(Objects::nonNull)
                .filter(post -> PUBLISHED.equals(post.getStatus()))
                .collect(Collectors.toList());
        Map<Long, AppUserSummary> users = usersForPosts(posts);
        List<PostCardVO> records = posts.stream().map(post -> toCard(post, users)).collect(Collectors.toList());
        return new PageResponse<>(records, page, pageSize, result.getTotalElements());
    }

    public List<PostCardVO> hotPosts() {
        List<ForumPost> posts = postRepository.findTop8ByStatusOrderByHotScoreDescPublishTimeDesc(PUBLISHED);
        Map<Long, AppUserSummary> users = usersForPosts(posts);
        return posts.stream()
                .map(post -> toCard(post, users))
                .collect(Collectors.toList());
    }

    public PostDetailVO detail(Long postId, Long userId, String identity) {
        ForumPost post = requireVisiblePost(postId);
        recordView(postId, userId, identity);
        PostDetailVO vo = toDetail(post, userDirectoryService.findNormalUsers(java.util.Collections.singleton(post.getUserId())));
        if (userId != null) {
            vo.setLiked(likeRepository.existsByUserIdAndTargetTypeAndTargetId(userId, POST, postId));
            vo.setFavorited(favoriteRepository.existsByUserIdAndPostId(userId, postId));
        }
        log.info("forum post detail postId={} userId={}", postId, userId);
        return vo;
    }

    @Transactional
    public PostDetailVO createPost(SavePostRequest request, Long userId) {
        requireEnabledBoard(request.getBoardId());
        ForumPost post = new ForumPost();
        fillPost(post, request);
        post.setUserId(userId);
        post.setStatus(PUBLISHED);
        post.setPublishTime(LocalDateTime.now());
        ForumPost saved = postRepository.save(post);
        log.info("forum post created userId={} postId={} boardId={} status={}", userId, saved.getId(), saved.getBoardId(), saved.getStatus());
        return toDetail(saved, userDirectoryService.findNormalUsers(java.util.Collections.singleton(saved.getUserId())));
    }

    @Transactional
    public PostDetailVO updatePost(Long postId, SavePostRequest request, Long userId, boolean admin) {
        ForumPost post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(404, "帖子不存在"));
        if (DELETED.equals(post.getStatus())) {
            throw new BusinessException(404, "帖子不存在");
        }
        if (!admin && !userId.equals(post.getUserId())) {
            throw new BusinessException(403, "只能编辑自己的帖子");
        }
        requireEnabledBoard(request.getBoardId());
        fillPost(post, request);
        ForumPost saved = postRepository.save(post);
        log.info("forum post updated userId={} postId={} admin={}", userId, postId, admin);
        return toDetail(saved, userDirectoryService.findNormalUsers(java.util.Collections.singleton(saved.getUserId())));
    }

    @Transactional
    public void deletePost(Long postId, Long userId, boolean admin) {
        ForumPost post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(404, "帖子不存在"));
        if (!admin && !userId.equals(post.getUserId())) {
            throw new BusinessException(403, "只能删除自己的帖子");
        }
        post.setStatus(DELETED);
        postRepository.save(post);
        log.info("forum post deleted userId={} postId={} admin={}", userId, postId, admin);
    }

    @Transactional
    public PostDetailVO updatePostStatus(Long postId, String status, Boolean topFlag, Boolean essenceFlag, Long adminId) {
        ForumPost post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(404, "帖子不存在"));
        if (StringUtils.hasText(status)) {
            post.setStatus(status.trim().toUpperCase());
        }
        if (topFlag != null) {
            post.setTopFlag(topFlag);
        }
        if (essenceFlag != null) {
            post.setEssenceFlag(essenceFlag);
        }
        ForumPost saved = postRepository.save(post);
        log.info("admin forum post status updated adminId={} postId={} status={} topFlag={} essenceFlag={}",
                adminId, postId, saved.getStatus(), saved.getTopFlag(), saved.getEssenceFlag());
        return toDetail(saved, userDirectoryService.findNormalUsers(java.util.Collections.singleton(saved.getUserId())));
    }

    @Transactional
    public CommentVO createComment(Long postId, SaveCommentRequest request, Long userId) {
        ForumPost post = requireVisiblePost(postId);
        if ("LOCKED".equals(post.getStatus())) {
            throw new BusinessException(403, "帖子已锁定，不能评论");
        }
        Long parentId = request.getParentId() == null ? 0L : request.getParentId();
        ForumComment comment = new ForumComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setContent(cleanText(request.getContent(), 2000));
        if (parentId != null && parentId > 0) {
            ForumComment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new BusinessException(404, "父评论不存在"));
            if (!postId.equals(parent.getPostId()) || !NORMAL.equals(parent.getStatus())) {
                throw new BusinessException(400, "父评论不属于当前帖子");
            }
            comment.setRootId(parent.getRootId());
            comment.setReplyToUserId(parent.getUserId());
        }
        ForumComment saved = commentRepository.save(comment);
        if (parentId == null || parentId <= 0) {
            saved.setRootId(saved.getId());
            saved = commentRepository.save(saved);
        }
        postRepository.increaseCommentCount(postId, 1L);
        log.info("forum comment created userId={} postId={} commentId={} rootId={} parentId={}",
                userId, postId, saved.getId(), saved.getRootId(), saved.getParentId());
        return toComment(saved, userId);
    }

    public PageResponse<CommentVO> comments(Long postId, Integer pageValue, Integer pageSizeValue, Long userId) {
        int page = normalizePage(pageValue);
        int pageSize = normalizePageSize(pageSizeValue);
        Page<ForumComment> result = commentRepository.findByPostIdAndParentIdAndStatusOrderByCreatedAtDesc(
                postId, 0L, NORMAL, PageRequest.of(page - 1, pageSize));
        List<ForumComment> comments = result.getContent();
        Map<Long, AppUserSummary> users = usersForComments(comments);
        List<CommentVO> roots = comments.stream().map(comment -> toComment(comment, userId, users)).collect(Collectors.toList());
        attachPreviewReplies(postId, roots, userId);
        return new PageResponse<>(roots, page, pageSize, result.getTotalElements());
    }

    public PageResponse<CommentVO> replies(Long rootCommentId, Integer pageValue, Integer pageSizeValue, Long userId) {
        ForumComment root = commentRepository.findById(rootCommentId)
                .orElseThrow(() -> new BusinessException(404, "评论不存在"));
        if (!NORMAL.equals(root.getStatus())) {
            throw new BusinessException(404, "评论不存在");
        }
        int page = normalizePage(pageValue);
        int pageSize = normalizePageSize(pageSizeValue);
        Page<ForumComment> result = commentRepository.findByPostIdAndRootIdAndParentIdNotAndStatusOrderByCreatedAtAsc(
                root.getPostId(), root.getRootId(), 0L, NORMAL, PageRequest.of(page - 1, pageSize));
        List<ForumComment> comments = result.getContent();
        Map<Long, AppUserSummary> users = usersForComments(comments);
        List<CommentVO> records = comments.stream().map(comment -> toComment(comment, userId, users)).collect(Collectors.toList());
        return new PageResponse<>(records, page, pageSize, result.getTotalElements());
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId, boolean admin) {
        ForumComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(404, "评论不存在"));
        if (!NORMAL.equals(comment.getStatus())) {
            return;
        }
        if (!admin && !userId.equals(comment.getUserId())) {
            throw new BusinessException(403, "只能删除自己的评论");
        }
        List<ForumComment> deleting = isRootComment(comment)
                ? commentRepository.findByPostIdAndRootIdAndStatus(comment.getPostId(), comment.getRootId(), NORMAL)
                : new ArrayList<>();
        if (!isRootComment(comment)) {
            deleting.add(comment);
        }
        for (ForumComment item : deleting) {
            item.setStatus(DELETED);
        }
        commentRepository.saveAll(deleting);
        if (!deleting.isEmpty()) {
            postRepository.increaseCommentCount(comment.getPostId(), -(long) deleting.size());
        }
        log.info("forum comment deleted userId={} commentId={} admin={} affected={}", userId, commentId, admin, deleting.size());
    }

    @Transactional
    public void likePost(Long postId, Long userId) {
        requireVisiblePost(postId);
        if (likeRepository.existsByUserIdAndTargetTypeAndTargetId(userId, POST, postId)) {
            return;
        }
        try {
            likeRepository.save(new ForumLike(userId, POST, postId));
            postRepository.increaseLikeCount(postId, 1L);
            log.info("forum post like success userId={} postId={}", userId, postId);
        } catch (DataIntegrityViolationException ignored) {
            log.info("forum post like duplicated userId={} postId={}", userId, postId);
        }
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
        long deleted = likeRepository.deleteByUserIdAndTargetTypeAndTargetId(userId, POST, postId);
        if (deleted > 0) {
            postRepository.increaseLikeCount(postId, -1L);
            log.info("forum post unlike success userId={} postId={}", userId, postId);
        }
    }

    @Transactional
    public void favoritePost(Long postId, Long userId) {
        requireVisiblePost(postId);
        if (favoriteRepository.existsByUserIdAndPostId(userId, postId)) {
            return;
        }
        try {
            favoriteRepository.save(new ForumFavorite(userId, postId));
            postRepository.increaseFavoriteCount(postId, 1L);
            log.info("forum post favorite success userId={} postId={}", userId, postId);
        } catch (DataIntegrityViolationException ignored) {
            log.info("forum post favorite duplicated userId={} postId={}", userId, postId);
        }
    }

    @Transactional
    public void unfavoritePost(Long postId, Long userId) {
        long deleted = favoriteRepository.deleteByUserIdAndPostId(userId, postId);
        if (deleted > 0) {
            postRepository.increaseFavoriteCount(postId, -1L);
            log.info("forum post unfavorite success userId={} postId={}", userId, postId);
        }
    }

    @Transactional
    public void likeComment(Long commentId, Long userId) {
        ForumComment comment = commentRepository.findById(commentId).orElseThrow(() -> new BusinessException(404, "评论不存在"));
        if (!NORMAL.equals(comment.getStatus())) {
            throw new BusinessException(404, "评论不存在");
        }
        if (!likeRepository.existsByUserIdAndTargetTypeAndTargetId(userId, COMMENT, commentId)) {
            try {
                likeRepository.save(new ForumLike(userId, COMMENT, commentId));
                commentRepository.increaseLikeCount(comment.getId(), 1L);
                log.info("forum comment like success userId={} commentId={}", userId, commentId);
            } catch (DataIntegrityViolationException ignored) {
                log.info("forum comment like duplicated userId={} commentId={}", userId, commentId);
            }
        }
    }

    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        long deleted = likeRepository.deleteByUserIdAndTargetTypeAndTargetId(userId, COMMENT, commentId);
        if (deleted > 0) {
            commentRepository.increaseLikeCount(commentId, -1L);
            log.info("forum comment unlike success userId={} commentId={}", userId, commentId);
        }
    }

    @Transactional
    public void flushViewCounts() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(VIEW_BUFFER_KEY);
        if (entries.isEmpty()) {
            return;
        }
        int flushed = 0;
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long postId = parseLong(entry.getKey());
            Long delta = parseLong(entry.getValue());
            if (postId == null || delta == null || delta <= 0) {
                continue;
            }
            int updated = postRepository.increaseViewCount(postId, delta);
            if (updated > 0) {
                Long remain = redisTemplate.opsForHash().increment(VIEW_BUFFER_KEY, String.valueOf(postId), -delta);
                if (remain != null && remain <= 0) {
                    redisTemplate.opsForHash().delete(VIEW_BUFFER_KEY, String.valueOf(postId));
                }
                flushed++;
            }
        }
        if (flushed > 0) {
            log.info("forum view count flushed postCount={}", flushed);
        }
    }

    private void fillPost(ForumPost post, SavePostRequest request) {
        post.setBoardId(request.getBoardId());
        post.setTitle(cleanText(request.getTitle(), 128));
        post.setContent(cleanContent(request.getContent()));
        post.setContentText(stripMarkdown(request.getContent()));
        post.setSummary(summary(post.getContentText()));
        post.setCoverUrl(cleanNullable(request.getCoverUrl()));
        post.setCoverObjectKey(cleanNullable(request.getCoverObjectKey()));
    }

    private Specification<ForumPost> buildSpecification(PostQueryRequest request, boolean admin) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (admin && StringUtils.hasText(request.getStatus())) {
                predicates.add(builder.equal(root.get("status"), request.getStatus().trim().toUpperCase()));
            } else {
                predicates.add(builder.equal(root.get("status"), PUBLISHED));
            }
            if (request.getBoardId() != null) {
                predicates.add(builder.equal(root.get("boardId"), request.getBoardId()));
            }
            if (StringUtils.hasText(request.getKeyword())) {
                String keyword = "%" + request.getKeyword().trim() + "%";
                predicates.add(builder.or(
                        builder.like(root.get("title"), keyword),
                        builder.like(root.get("summary"), keyword),
                        builder.like(root.get("contentText"), keyword)
                ));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort buildSort(String sort) {
        if ("hot".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "topFlag", "hotScore", "publishTime");
        }
        if ("mostViewed".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "topFlag", "viewCount", "publishTime");
        }
        if ("mostCommented".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "topFlag", "commentCount", "publishTime");
        }
        if ("mostLiked".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "topFlag", "likeCount", "publishTime");
        }
        return Sort.by(Sort.Direction.DESC, "topFlag", "publishTime", "createdAt");
    }

    private ForumPost requireVisiblePost(Long postId) {
        if (postId == null || postId <= 0) {
            throw new BusinessException(400, "帖子ID不合法");
        }
        ForumPost post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(404, "帖子不存在"));
        if (!PUBLISHED.equals(post.getStatus()) && !"LOCKED".equals(post.getStatus())) {
            throw new BusinessException(404, "帖子不存在或不可见");
        }
        return post;
    }

    private void requireEnabledBoard(Long boardId) {
        if (boardId == null || !boardRepository.existsById(boardId)) {
            throw new BusinessException(404, "论坛板块不存在");
        }
    }

    private void recordView(Long postId, Long userId, String identity) {
        String visitor = userId != null ? "u" + userId : "g" + Math.abs(Objects.toString(identity, "anonymous").hashCode());
        String dedupKey = "forum:post:view:dedup:" + postId + ":" + visitor;
        Boolean inserted = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1", Duration.ofMinutes(viewDedupMinutes));
        if (Boolean.FALSE.equals(inserted)) {
            return;
        }
        redisTemplate.opsForHash().increment(VIEW_BUFFER_KEY, String.valueOf(postId), 1L);
        redisTemplate.opsForZSet().incrementScore("forum:post:hot:total", String.valueOf(postId), 1D);
    }

    private void attachPreviewReplies(Long postId, List<CommentVO> roots, Long userId) {
        if (roots.isEmpty()) {
            return;
        }
        List<Long> rootIds = roots.stream().map(CommentVO::getRootId).collect(Collectors.toList());
        List<ForumComment> replies = commentRepository.findByPostIdAndRootIdInAndParentIdNotAndStatusOrderByCreatedAtAsc(postId, rootIds, 0L, NORMAL);
        Map<Long, AppUserSummary> users = usersForComments(replies);
        Map<Long, List<CommentVO>> grouped = new LinkedHashMap<>();
        for (ForumComment reply : replies) {
            List<CommentVO> values = grouped.computeIfAbsent(reply.getRootId(), ignored -> new ArrayList<>());
            if (values.size() < 3) {
                values.add(toComment(reply, userId, users));
            }
        }
        roots.forEach(root -> root.setReplies(grouped.getOrDefault(root.getRootId(), new ArrayList<>())));
    }

    private PostCardVO toCard(ForumPost post, Map<Long, AppUserSummary> users) {
        PostCardVO vo = new PostCardVO();
        AppUserSummary author = users.get(post.getUserId());
        vo.setId(post.getId());
        vo.setBoardId(post.getBoardId());
        vo.setBoardName(boardService.resolveBoardName(post.getBoardId()));
        vo.setUserId(post.getUserId());
        vo.setAuthorName(userDirectoryService.displayName(author, post.getUserId()));
        vo.setAuthorAvatar(author == null ? null : author.getAvatar());
        vo.setTitle(post.getTitle());
        vo.setSummary(post.getSummary());
        vo.setCoverUrl(post.getCoverUrl());
        vo.setViewCount(nonNull(post.getViewCount()));
        vo.setLikeCount(nonNull(post.getLikeCount()));
        vo.setCommentCount(nonNull(post.getCommentCount()));
        vo.setFavoriteCount(nonNull(post.getFavoriteCount()));
        vo.setTopFlag(Boolean.TRUE.equals(post.getTopFlag()));
        vo.setEssenceFlag(Boolean.TRUE.equals(post.getEssenceFlag()));
        vo.setStatus(post.getStatus());
        vo.setPublishTime(post.getPublishTime());
        vo.setCreatedAt(post.getCreatedAt());
        return vo;
    }

    private PostDetailVO toDetail(ForumPost post, Map<Long, AppUserSummary> users) {
        PostDetailVO vo = new PostDetailVO();
        PostCardVO card = toCard(post, users);
        vo.setId(card.getId());
        vo.setBoardId(card.getBoardId());
        vo.setBoardName(card.getBoardName());
        vo.setUserId(card.getUserId());
        vo.setAuthorName(card.getAuthorName());
        vo.setAuthorAvatar(card.getAuthorAvatar());
        vo.setTitle(card.getTitle());
        vo.setSummary(card.getSummary());
        vo.setCoverUrl(card.getCoverUrl());
        vo.setViewCount(card.getViewCount());
        vo.setLikeCount(card.getLikeCount());
        vo.setCommentCount(card.getCommentCount());
        vo.setFavoriteCount(card.getFavoriteCount());
        vo.setTopFlag(card.getTopFlag());
        vo.setEssenceFlag(card.getEssenceFlag());
        vo.setStatus(card.getStatus());
        vo.setPublishTime(card.getPublishTime());
        vo.setCreatedAt(card.getCreatedAt());
        vo.setContent(post.getContent());
        return vo;
    }

    private CommentVO toComment(ForumComment comment, Long userId) {
        return toComment(comment, userId, usersForComments(java.util.Collections.singleton(comment)));
    }

    private CommentVO toComment(ForumComment comment, Long userId, Map<Long, AppUserSummary> users) {
        CommentVO vo = new CommentVO();
        AppUserSummary author = users.get(comment.getUserId());
        AppUserSummary replyTo = users.get(comment.getReplyToUserId());
        vo.setId(comment.getId());
        vo.setPostId(comment.getPostId());
        vo.setUserId(comment.getUserId());
        vo.setAuthorName(userDirectoryService.displayName(author, comment.getUserId()));
        vo.setAuthorAvatar(author == null ? null : author.getAvatar());
        vo.setParentId(comment.getParentId());
        vo.setRootId(comment.getRootId());
        vo.setReplyToUserId(comment.getReplyToUserId());
        if (comment.getReplyToUserId() != null) {
            vo.setReplyToUserName(userDirectoryService.displayName(replyTo, comment.getReplyToUserId()));
            vo.setReplyToUserAvatar(replyTo == null ? null : replyTo.getAvatar());
        }
        vo.setContent(comment.getContent());
        vo.setLikeCount(nonNull(comment.getLikeCount()));
        vo.setCreatedAt(comment.getCreatedAt());
        if (userId != null) {
            vo.setLiked(likeRepository.existsByUserIdAndTargetTypeAndTargetId(userId, COMMENT, comment.getId()));
        }
        return vo;
    }

    private Map<Long, AppUserSummary> usersForPosts(Collection<ForumPost> posts) {
        if (posts == null || posts.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        List<Long> userIds = posts.stream()
                .map(ForumPost::getUserId)
                .collect(Collectors.toList());
        return userDirectoryService.findNormalUsers(userIds);
    }

    private Map<Long, AppUserSummary> usersForComments(Collection<ForumComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        List<Long> userIds = new ArrayList<>();
        for (ForumComment comment : comments) {
            userIds.add(comment.getUserId());
            userIds.add(comment.getReplyToUserId());
        }
        return userDirectoryService.findNormalUsers(userIds);
    }

    private String cleanContent(String value) {
        String cleaned = cleanText(value, 20000);
        return cleaned.replace("<script", "&lt;script").replace("</script>", "&lt;/script&gt;");
    }

    private String stripMarkdown(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replaceAll("[#>*_`\\[\\]()]"," ").replaceAll("\\s+", " ").trim();
    }

    private String summary(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String clean = value.trim();
        return clean.length() <= 160 ? clean : clean.substring(0, 160) + "...";
    }

    private String cleanText(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, "内容不能为空");
        }
        String clean = value.trim();
        if (clean.length() > maxLength) {
            return clean.substring(0, maxLength);
        }
        return clean;
    }

    private String cleanNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private long nonNull(Long value) {
        return value == null ? 0L : value;
    }

    private Long parseLong(Object value) {
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean isRootComment(ForumComment comment) {
        return comment.getParentId() == null || comment.getParentId() <= 0 || Objects.equals(comment.getId(), comment.getRootId());
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
}
