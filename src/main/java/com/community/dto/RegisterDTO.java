package com.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求参数
 */
@Data
public class RegisterDTO {

    /** 登录名（唯一，不能为空） */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码（6-20 位） */
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String password;

    /** 昵称（显示名，不能为空） */
    @NotBlank(message = "昵称不能为空")
    private String nickname;
}
