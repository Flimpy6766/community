package com.community.admin.dto.request;

import jakarta.validation.constraints.NotNull;

public class UpdateUserStatusDTO {
    /** 新状态：0 禁用 / 1 启用 */
    @NotNull(message = "状态不能为空")
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
