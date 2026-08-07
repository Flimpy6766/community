package com.community.service;

import com.community.dto.LoginDTO;
import com.community.dto.LoginVO;
import com.community.dto.RegisterDTO;

public interface UserService {

    void register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);
}
