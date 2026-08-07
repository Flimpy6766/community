package com.community.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {

    private Long id;
    private Long userId;
    private String nickname;
    private String content;
    private LocalDateTime createTime;
    private List<CommentVO> replies;   // 回复（和顶级同构——回复也是评论）
}
