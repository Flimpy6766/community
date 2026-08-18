package com.community.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.admin.dto.response.AdminArticleVO;
import com.community.admin.dto.response.AdminOverviewVO;
import com.community.admin.mapper.AdminMapper;
import com.community.admin.service.AdminQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminQueryServiceImpl implements AdminQueryService {

    private final AdminMapper adminMapper;

    public AdminQueryServiceImpl(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOverviewVO overview() {
        return adminMapper.selectOverview();
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<AdminArticleVO> listAll(Integer page, Integer size) {
        return adminMapper.selectAdminArticles(new Page<>(page, size));
    }
}
