package com.community.common.result;

import lombok.Data;

@Data
public class Result<T> {
    /*code = 0 表示成功，非 0 表示失败。*/
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        return build(0, "success", null);
    }

    public static <T> Result<T> success(T data) { return build(0, "success", data); }

    public static <T> Result<T> error(Integer code, String message) { return build(code, message, null); }

    private static <T> Result<T> build(Integer code, String message, T data) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        r.data = data;
        return r;
    }
}
