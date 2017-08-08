package com.tacademy.semo.item;

public class RecommendKeyword {

    public int keyword_id;
    public String keyword;
    public boolean owned;

    public RecommendKeyword() {

    }

    public RecommendKeyword(String keyword, boolean owned) {
        this.keyword = keyword;
        this.owned = owned;
    }
}
