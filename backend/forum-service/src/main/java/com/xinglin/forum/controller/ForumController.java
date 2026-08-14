package com.xinglin.forum.controller;

import com.xinglin.forum.common.ApiResponse;
import com.xinglin.forum.common.PageResponse;
import com.xinglin.forum.dto.PostQueryRequest;
import com.xinglin.forum.dto.SaveCommentRequest;
import com.xinglin.forum.dto.SavePostRequest;
import com.xinglin.forum.security.AuthenticatedUser;
import com.xinglin.forum.security.ForumAuthService;
import com.xinglin.forum.service.ForumBoardService;
import com.xinglin.forum.service.ForumService;
import com.xinglin.forum.vo.BoardVO;
import com.xinglin.forum.vo.CommentVO;
import com.xinglin.forum.vo.PostCardVO;
import com.xinglin.forum.vo.PostDetailVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/forum")
public class ForumController {
    private final ForumBoardService boardService;
    private final ForumService forumService;
    private final ForumAuthService authService;

    public ForumController(ForumBoardService boardService, ForumService forumService, ForumAuthService authService) {
        this.boardService = boardService;
        this.forumService = forumService;
        this.authService = authService;
    }

    @GetMapping("/boards")
    public ApiResponse<List<BoardVO>> boards() {
        return ApiResponse.success(boardService.listEnabledBoards());
    }

    @GetMapping("/posts")
    public ApiResponse<PageResponse<PostCardVO>> posts(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                       PostQueryRequest request) {
        Long userId = authService.optionalUserId(authorization);
        return ApiResponse.success(forumService.queryPosts(request, userId, false));
    }

    @GetMapping("/posts/hot")
    public ApiResponse<List<PostCardVO>> hotPosts() {
        return ApiResponse.success(forumService.hotPosts());
    }

    @GetMapping("/posts/{postId}")
    public ApiResponse<PostDetailVO> detail(@RequestHeader(value = "Authorization", required = false) String authorization,
                                            @PathVariable Long postId,
                                            HttpServletRequest request) {
        Long userId = authService.optionalUserId(authorization);
        return ApiResponse.success(forumService.detail(postId, userId, identity(request)));
    }

    @PostMapping("/posts")
    public ApiResponse<PostDetailVO> createPost(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                @Validated @RequestBody SavePostRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(forumService.createPost(request, user.getUserId()));
    }

    @PutMapping("/posts/{postId}")
    public ApiResponse<PostDetailVO> updatePost(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                @PathVariable Long postId,
                                                @Validated @RequestBody SavePostRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(forumService.updatePost(postId, request, user.getUserId(), user.hasRole("ADMIN")));
    }

    @DeleteMapping("/posts/{postId}")
    public ApiResponse<Void> deletePost(@RequestHeader(value = "Authorization", required = false) String authorization,
                                        @PathVariable Long postId) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        forumService.deletePost(postId, user.getUserId(), user.hasRole("ADMIN"));
        return ApiResponse.success(null);
    }

    @GetMapping("/my/posts")
    public ApiResponse<PageResponse<PostCardVO>> myPosts(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                         PostQueryRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(forumService.myPosts(request, user.getUserId()));
    }

    @GetMapping("/my/favorites")
    public ApiResponse<PageResponse<PostCardVO>> myFavorites(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                             PostQueryRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(forumService.myFavorites(request, user.getUserId()));
    }

    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<CommentVO> createComment(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                @PathVariable Long postId,
                                                @Validated @RequestBody SaveCommentRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(forumService.createComment(postId, request, user.getUserId()));
    }

    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<PageResponse<CommentVO>> comments(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                         @PathVariable Long postId,
                                                         @RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = authService.optionalUserId(authorization);
        return ApiResponse.success(forumService.comments(postId, page, pageSize, userId));
    }

    @GetMapping("/comments/{rootCommentId}/replies")
    public ApiResponse<PageResponse<CommentVO>> replies(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                        @PathVariable Long rootCommentId,
                                                        @RequestParam(defaultValue = "1") Integer page,
                                                        @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = authService.optionalUserId(authorization);
        return ApiResponse.success(forumService.replies(rootCommentId, page, pageSize, userId));
    }

    @PostMapping("/posts/{postId}/like")
    public ApiResponse<Void> likePost(@RequestHeader(value = "Authorization", required = false) String authorization,
                                      @PathVariable Long postId) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        forumService.likePost(postId, user.getUserId());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/posts/{postId}/like")
    public ApiResponse<Void> unlikePost(@RequestHeader(value = "Authorization", required = false) String authorization,
                                        @PathVariable Long postId) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        forumService.unlikePost(postId, user.getUserId());
        return ApiResponse.success(null);
    }

    @PostMapping("/posts/{postId}/favorite")
    public ApiResponse<Void> favoritePost(@RequestHeader(value = "Authorization", required = false) String authorization,
                                          @PathVariable Long postId) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        forumService.favoritePost(postId, user.getUserId());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/posts/{postId}/favorite")
    public ApiResponse<Void> unfavoritePost(@RequestHeader(value = "Authorization", required = false) String authorization,
                                            @PathVariable Long postId) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        forumService.unfavoritePost(postId, user.getUserId());
        return ApiResponse.success(null);
    }

    @PostMapping("/comments/{commentId}/like")
    public ApiResponse<Void> likeComment(@RequestHeader(value = "Authorization", required = false) String authorization,
                                         @PathVariable Long commentId) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        forumService.likeComment(commentId, user.getUserId());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/comments/{commentId}/like")
    public ApiResponse<Void> unlikeComment(@RequestHeader(value = "Authorization", required = false) String authorization,
                                           @PathVariable Long commentId) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        forumService.unlikeComment(commentId, user.getUserId());
        return ApiResponse.success(null);
    }

    private String identity(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            return forwarded.split(",")[0].trim() + ":" + request.getHeader("User-Agent");
        }
        return request.getRemoteAddr() + ":" + request.getHeader("User-Agent");
    }
}
