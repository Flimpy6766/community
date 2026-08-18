package com.community.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.admin.dto.response.AdminArticleVO;
import com.community.admin.dto.response.AdminOverviewVO;

public interface AdminQueryService {

    AdminOverviewVO overview();

    IPage<AdminArticleVO> listAll(Integer page, Integer size);
}
