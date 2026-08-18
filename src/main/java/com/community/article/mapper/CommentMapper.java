package com.community.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.article.entity.Comment;
import com.community.user.dto.response.response.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    IPage<CommentVO> selectTopCommentVOs(Page<CommentVO> page,
                                         @Param("articleId") Long articleId);

    List<CommentVO> selectReplyVOs(@Param("articleId") Long articleId,
                                   @Param("parentIds") List<Long> parentIds);
}
