package com.community.common.result;


public class Result<T> {
    /*code = 0 表示成功，非 0 表示失败。*/
    private Integer code;
    private String message;
    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

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
