package com.tacademy.semo.item;

import java.util.ArrayList;

public class ShareMemo {

    public int shareId;
    public boolean pin;
    public Memo memo;
    public String createdAt;
    public ArrayList<User> coAuthorList;
    public boolean selected;

    public ShareMemo() {
    }
    // 공유친구 추가할때,
    public ShareMemo(int shareId, boolean pin, Memo memo, String createdAt, ArrayList<User> coAuthorList) {
        this.shareId = shareId;
        this.pin = pin;
        this.memo = memo;
        this.createdAt = createdAt;
        this.coAuthorList = coAuthorList;
        selected = false;
    }
}
