package com.community.admin.service;

import com.community.admin.dto.request.UpdateUserStatusDTO;

public interface AdminService {
    void updateUserStatus(Long id, UpdateUserStatusDTO dto);

    void deleteArticle(Long id);
    void deleteComment(Long id);
}
