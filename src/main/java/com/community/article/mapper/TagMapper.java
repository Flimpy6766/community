package com.community.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.article.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    int insertIgnoreBatch(@Param("names") List<String> names);

    List<Tag> selectByNames(@Param("names") List<String> names);
}
