package com.xinglin.ai.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        log.warn("business exception code={} message={}", ex.getCode(), ex.getMessage());
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(Exception ex) {
        String message = resolveValidationMessage(ex);
        log.warn("validation failed message={} detail={}", message, ex.getMessage());
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception ex) {
        log.error("unhandled exception", ex);
        return ApiResponse.fail(500, "服务暂时不可用，请稍后重试");
    }

    private String resolveValidationMessage(Exception ex) {
        BindingResult bindingResult = null;
        if (ex instanceof MethodArgumentNotValidException) {
            bindingResult = ((MethodArgumentNotValidException) ex).getBindingResult();
        } else if (ex instanceof BindException) {
            bindingResult = ((BindException) ex).getBindingResult();
        }
        if (bindingResult != null && bindingResult.hasFieldErrors()) {
            return bindingResult.getFieldErrors().get(0).getDefaultMessage();
        }
        return "请求参数不合法";
    }
}
