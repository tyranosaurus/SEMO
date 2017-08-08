package com.tacademy.semo.item;

public class Comment {

    public int replyId;
    public String body;
    public String createdAt;
    public String modifiedAt;
    public User user;

    public Comment(int replyId, String body, String createdAt, String modifiedAt, User user) {
        this.replyId = replyId;
        this.body = body;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.user = user;
    }
}