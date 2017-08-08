package com.tacademy.semo.item;

public class BestMemo {

    public int rank;
    public int memoId;
    public String body;
    public boolean shared;
    public boolean hidden;
    public boolean deleted;
    public boolean pin;
    public String createdAt;
    public int connectionCount;
    public int userId;
    public User user;
    public String keyword = "";

    public BestMemo() {
    }

    public BestMemo(int rank, int memoId, String body, boolean shared, boolean hidden, boolean deleted,
                    boolean pin, String createdAt, int connectionCount, int userId, User user, String keyword) {
        this.rank = rank;
        this.memoId = memoId;
        this.body = body;
        this.shared = shared;
        this.hidden = hidden;
        this.deleted = deleted;
        this.pin = pin;
        this.createdAt = createdAt;
        this.connectionCount = connectionCount;
        this.userId = userId;
        this.user = user;
        this.keyword = keyword;
    }
}
