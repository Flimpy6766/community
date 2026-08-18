package com.community.user.dto.response.response;

import java.time.LocalDateTime;
import java.util.List;

public class CommentVO {

    private Long id;
    /** 回复所属的顶级评论 id；顶级评论为 0 */
    private Long parentId;
    private Long userId;
    private String nickname;
    private String content;
    private LocalDateTime createTime;
    private List<CommentVO> replies;   // 回复（和顶级同构——回复也是评论）

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public List<CommentVO> getReplies() {
        return replies;
    }

    public void setReplies(List<CommentVO> replies) {
        this.replies = replies;
    }
}
