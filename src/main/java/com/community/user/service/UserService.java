package com.community.user.service;

import com.community.user.dto.request.LoginDTO;
import com.community.user.dto.request.RegisterDTO;
import com.community.user.dto.request.UpdateProfileDTO;
import com.community.user.dto.response.LoginVO;
import com.community.user.dto.response.UserProfileVO;

public interface UserService {

    void register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    UserProfileVO profile();
    void updateProfile(UpdateProfileDTO dto);
}
