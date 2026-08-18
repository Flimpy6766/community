package com.community.admin.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.admin.dto.response.AdminArticleVO;
import com.community.admin.dto.response.AdminOverviewVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {

    AdminOverviewVO selectOverview();

    IPage<AdminArticleVO> selectAdminArticles(Page<AdminArticleVO> page);
}
