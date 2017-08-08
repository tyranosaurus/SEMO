package com.tacademy.semo.item;

public class DtailParentMemo {

    public int memo_id;
    public String body;
    public boolean shared;
    public boolean hidden;
    public boolean deleted;
    public boolean pin;
    public String modifiedAt;
    public String createdAt;
    public int connectionCount;
    public int user_id;
    public String userNickname;
    public int order;
    public String keyword = "";
    // 타겟메모의 부보메모 가져올때, 부모메모의 부모메모 가져올때
    public DtailParentMemo (int memo_id, String body, boolean shared, boolean hidden, boolean deleted, boolean pin, String modifiedAt,
                            String createdAt, int connectionCount, int user_id, String userNickname, int order, String keyword) {
        this.memo_id = memo_id;
        this.body = body;
        this.shared = shared;
        this.hidden = hidden;
        this.deleted = deleted;
        this.pin = pin;
        this.modifiedAt = modifiedAt;
        this.createdAt = createdAt;
        this.connectionCount = connectionCount;
        this.user_id = user_id;
        this.userNickname = userNickname;
        this.order = order;
        this.keyword = keyword;
    }
}
