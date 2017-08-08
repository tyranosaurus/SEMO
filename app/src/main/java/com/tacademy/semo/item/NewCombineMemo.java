package com.tacademy.semo.item;

public class NewCombineMemo {

    public int memoId;
    public String body;
    public String nickname;
    public int userId;
    public boolean selected;
    public String createdAt;
    public int connectionCount;

    public NewCombineMemo() {
    }

    public NewCombineMemo(int memoId, String body, String nickname, int userId, String createdAt, int connectionCount) {
        this.memoId = memoId;
        this.body = body;
        this.nickname = nickname;
        this.userId = userId;
        this.createdAt = createdAt;
        this.connectionCount = connectionCount;

        selected = false;
    }
}
