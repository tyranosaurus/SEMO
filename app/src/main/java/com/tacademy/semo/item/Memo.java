package com.tacademy.semo.item;

import java.io.Serializable;

public class Memo implements Serializable{
    // 메모 아이디
    public int memo_id;
    // 메모 핀 상태
    public boolean pin;
    // 메모 내용
    public String body;
    // 메모 공유
    public boolean shared;
    // 수정한 날짜
    public String modifiedAt;
    // 최초 작성 날짜
    public String createdAt;
    // 유저 아이디
    public int userId;
    // 오리지널 작성자
    public User origin_author;
    // 선택여부
    public boolean selected;
    // 키워드
    public String keyword = "";

    public Memo() {
    }

    // 상세보기 수정 메모 리스트
    public Memo(int memo_id, String body) {
        this.memo_id = memo_id;
        this.body = body;
    }
    // 삭제할 메모 리스트
    public Memo(int memo_id, boolean pin, String body) {
        this.memo_id = memo_id;
        this.pin = pin;
        this.body = body;
        this.selected = false;
    }
    // 공유취소, 새로운 메모 삽입
    public Memo(int memo_id, boolean pin, String body, String createdAt, int userId, User origin_author) {
        this.memo_id = memo_id;
        this.pin = pin;
        this.body = body;
        this.createdAt = createdAt;
        this.userId = userId;
        this.origin_author = origin_author;
        this.selected = false;
    }
    // 새로운 공유메모 삽입할 때
    public Memo(int memo_id, String body, boolean shared, String createdAt, int userId, String keyword) {
        this.memo_id = memo_id;
        this.body = body;
        this.shared = shared;
        this.createdAt = createdAt;
        this.userId = userId;
        this.selected = false;
        this.keyword = keyword;
    }
    // 공유메모 가져올때
    public Memo(int memo_id, String body, boolean shared, String createdAt, int userId) {
        this.memo_id = memo_id;
        this.body = body;
        this.shared = shared;
        this.createdAt = createdAt;
        this.userId = userId;
        this.selected = false;
    }
    // 유저메모 가져올 때
    public Memo(int memo_id, boolean pin, String body, String createdAt, int userId, User origin_author, String keyword) {
        this.memo_id = memo_id;
        this.pin = pin;
        this.body = body;
        this.createdAt = createdAt;
        this.userId = userId;
        this.origin_author = origin_author;
        this.selected = false;
        this.keyword = keyword;
    }
}
