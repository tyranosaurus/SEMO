package com.tacademy.semo.item;

public class HiddenMemo {
    public int parent_memo_id;
    public int user_id;
    public String body;
    public int ordernum;

    public HiddenMemo(int parent_memo_id, int user_id, String body, int ordernum) {
        this.parent_memo_id = parent_memo_id;
        this.user_id = user_id;
        this.body = body;
        this.ordernum = ordernum;
    }
}
