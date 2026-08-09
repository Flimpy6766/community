package com.community.service;

import com.community.dto.*;

public interface UserService {

    void register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    UserProfileVO profile();
    void updateProfile(UpdateProfileDTO dto);
}
