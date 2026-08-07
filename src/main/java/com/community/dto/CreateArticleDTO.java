package com.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateArticleDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长100字")
    private String title;

    private String summary;

    private String content;

    private String cover;

    /** 0草稿 1发布，不传默认 0 */
    private Integer status;

    /** 标签名列表，如 ["Java","Spring"] */
    private List<String> tags;
}