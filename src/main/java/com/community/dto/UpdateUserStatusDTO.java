package com.community.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusDTO {
    /** 新状态：0 禁用 / 1 启用 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
