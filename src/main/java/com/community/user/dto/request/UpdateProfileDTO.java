package com.community.user.dto.request;

import jakarta.validation.constraints.Size;

/** 修改个人资料请求参数（部分更新：传什么改什么） */
public class UpdateProfileDTO {
    /** 新昵称 */
    @Size(max = 20, message = "昵称最长20个字符")
    private String nickname;

    /** 新头像 URL */
    @Size(max = 500, message = "头像URL最长500个字符")
    private String avatar;

    /** 新简介 */
    @Size(max = 200, message = "简介最长200个字符")
    private String bio;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
