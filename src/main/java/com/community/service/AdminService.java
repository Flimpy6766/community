package com.community.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.dto.AdminArticleVO;
import com.community.dto.AdminOverviewVO;
import com.community.dto.UpdateUserStatusDTO;

public interface AdminService {
    AdminOverviewVO overview();
    IPage<AdminArticleVO> listAll(Integer page, Integer size);

    void updateUserStatus(Long id, UpdateUserStatusDTO dto);

    void deleteArticle(Long id);
    void deleteComment(Long id);
}
