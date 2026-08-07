package com.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentDTO {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论最长2000字")
    private String content;

    /** 0=顶级评论，非0=回复的评论id。不传默认顶级 */
    private Long parentId = 0L;
}
