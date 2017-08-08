package com.tacademy.semo.item;

import java.io.Serializable;

public class User implements Serializable {
    public int userId;
    public String nickName;
    public String profileImage;
    public String email;
    public String token;

    public User() {
    }

    public User(int userId, String nickName, String profileImage) {
        this.userId = userId;
        this.nickName = nickName;
        this.profileImage = profileImage;
    }

    // 공유 친구 목록 생성자
    public User(int userId, String nickName, String profileImage, String email) {
        this.userId = userId;
        this.nickName = nickName;
        this.profileImage = profileImage;
        this.email = email;
    }

    // 댓글 유저 생성자
    public User(int userId, String nickName, String profileImage, String email, String token) {
        this.userId = userId;
        this.nickName = nickName;
        this.profileImage = profileImage;
        this.email = email;
        this.token = token;
    }
}
