package com.xinglin.user.controller;

import com.xinglin.user.common.ApiResponse;
import com.xinglin.user.common.PageResponse;
import com.xinglin.user.common.UserContext;
import com.xinglin.user.service.PointsService;
import com.xinglin.user.vo.PointsAccountVO;
import com.xinglin.user.vo.PointsRecordVO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/points")
public class PointsController {
    private final PointsService pointsService;

    public PointsController(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    @GetMapping("/account")
    public ApiResponse<PointsAccountVO> account() {
        return ApiResponse.success(pointsService.account(UserContext.getUserId()));
    }

    @GetMapping("/records")
    public ApiResponse<PageResponse<PointsRecordVO>> records(@RequestParam(defaultValue = "1") Integer page,
                                                             @RequestParam(defaultValue = "20") Integer pageSize) {
        return ApiResponse.success(pointsService.records(UserContext.getUserId(), page, pageSize));
    }
}
