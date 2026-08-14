package com.xinglin.forum.controller;

import com.xinglin.forum.common.ApiResponse;
import com.xinglin.forum.common.PageResponse;
import com.xinglin.forum.dto.AdminPostStatusRequest;
import com.xinglin.forum.dto.PostQueryRequest;
import com.xinglin.forum.security.AuthenticatedUser;
import com.xinglin.forum.security.ForumAuthService;
import com.xinglin.forum.service.ForumService;
import com.xinglin.forum.vo.PostCardVO;
import com.xinglin.forum.vo.PostDetailVO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forum/admin")
public class AdminForumController {
    private final ForumService forumService;
    private final ForumAuthService authService;

    public AdminForumController(ForumService forumService, ForumAuthService authService) {
        this.forumService = forumService;
        this.authService = authService;
    }

    @GetMapping("/posts")
    public ApiResponse<PageResponse<PostCardVO>> posts(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                       PostQueryRequest request) {
        AuthenticatedUser admin = authService.requireAdmin(authorization);
        return ApiResponse.success(forumService.queryPosts(request, admin.getUserId(), true));
    }

    @PostMapping("/posts/{postId}/status")
    public ApiResponse<PostDetailVO> updateStatus(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  @PathVariable Long postId,
                                                  @RequestBody AdminPostStatusRequest request) {
        AuthenticatedUser admin = authService.requireAdmin(authorization);
        return ApiResponse.success(forumService.updatePostStatus(
                postId, request.getStatus(), request.getTopFlag(), request.getEssenceFlag(), admin.getUserId()));
    }

    @DeleteMapping("/posts/{postId}")
    public ApiResponse<Void> deletePost(@RequestHeader(value = "Authorization", required = false) String authorization,
                                        @PathVariable Long postId) {
        AuthenticatedUser admin = authService.requireAdmin(authorization);
        forumService.deletePost(postId, admin.getUserId(), true);
        return ApiResponse.success(null);
    }
}
