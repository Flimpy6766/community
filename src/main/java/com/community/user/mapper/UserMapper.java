package com.community.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.article.mapper.row.UserProfileStats;
import com.community.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    UserProfileStats selectProfileStats(@Param("userId") Long userId);
}
