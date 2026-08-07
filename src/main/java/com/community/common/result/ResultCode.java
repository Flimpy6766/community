package com.community.common.result;


import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(0, "success"),
    FAILED(500, "操作失败"),
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    SERVER_ERROR(500, "服务器内部错误");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message){
        this.code = code;
        this.message = message;
    }
}
