package com.xinglin.user.controller;

import com.xinglin.user.common.ApiResponse;
import com.xinglin.user.common.RequestTraceFilter;
import com.xinglin.user.common.UserContext;
import com.xinglin.user.service.CheckinService;
import com.xinglin.user.vo.CheckinCalendarVO;
import com.xinglin.user.vo.CheckinVO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user/checkin")
public class CheckinController {
    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @PostMapping
    public ApiResponse<CheckinVO> checkin(HttpServletRequest servletRequest) {
        return ApiResponse.success(checkinService.checkin(
                UserContext.getUserId(),
                RequestTraceFilter.clientIp(servletRequest),
                servletRequest.getHeader("User-Agent")));
    }

    @GetMapping("/calendar")
    public ApiResponse<CheckinCalendarVO> calendar(@RequestParam(required = false) String month) {
        return ApiResponse.success(checkinService.calendar(UserContext.getUserId(), month));
    }
}
