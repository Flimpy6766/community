package com.community.dto;

import lombok.Data;

@Data
public class LikeVO {

    /* 当前用户是否点赞 */
    private Boolean liked;

    /* 最新点赞数 */
    private Integer likeCount;
}
