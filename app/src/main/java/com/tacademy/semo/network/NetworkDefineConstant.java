package com.tacademy.semo.network;

public class NetworkDefineConstant {

    public static final String HOST_URL = "http://52.78.88.71:3000";

    //요청 URL path
    // 검색
    public static String SERVER_URL_SEARCH_MEMO;
    // 메모
    public static String SERVER_URL_INSERT_MEMO;
    public static String SERVER_URL_SELECT_USER_MEMO;
    public static String SERVER_URL_UPDATE_MEMO_PIN;
    public static String SERVER_URL_DELETE_MEMO;
    // 공유메모
    public static String SERVER_URL_SELECT_SHARE_MEMO;
    public static String SERVER_URL_SEARCH_USER;
    public static String SERVER_URL_INSERT_SHARE_USER;
    public static String SERVER_URL_UPDATE_SHARE_MEMO_PIN;
    public static String SERVER_URL_CANCEL_SHARE_MEMO;
    public static String SERVER_URL_RECENT_SHARE_USER;
    // 베스트메모
    public static String SERVER_URL_BEST_MEMO;
    // 아이데이션
    public static String SERVER_URL_SELECT_IDEATION_MEMO;
    public static String SERVER_URL_SELECT_COMBINE_MEMO;
    public static String SERVER_URL_INSERT_COMBINE_MEMO;
    public static String SERVER_URL_INSERT_HIDDEN_MEMO;
    // 상세보기
    public static String SERVER_URL_SELECT_DETAIL_MEMO;
    public static String SERVER_URL_UPDATE_DERAIL_MEMO;
    public static String SERVER_URL_INSERT_COMMENT;
    public static String SERVER_URL_CANCEL_COMBINE_MEMO;
    // 키워드
    public static String SERVER_URL_INSERT_KEYWORD;
    public static String SERVER_URL_SELECT_ALL_KEYWORD;
    public static String SERVER_URL_SELECT_USER_KEYWORD;
    public static String SERVER_URL_DELETE_KEYWORD;
    public static String SERVER_URL_ATTACH_KEYWORD;
    // 휴지통
    public static String SERVER_URL_SELECT_DELETE_MEMO;
    public static String SERVER_URL_DELETE_SELECTED_MEMO;
    public static String SERVER_URL_DELETE_ALL_MEMO;
    // FCM
    public static String SERVER_URL_SEND_FCM_TOKEN;

    static {
        // 검색
        SERVER_URL_SEARCH_MEMO = HOST_URL + "/memos?user_id=";
        // 메모
        SERVER_URL_INSERT_MEMO = HOST_URL + "/memos";
        SERVER_URL_SELECT_USER_MEMO = HOST_URL + "/memos?user_id=";
        SERVER_URL_UPDATE_MEMO_PIN = HOST_URL + "/memos/pin/";
        SERVER_URL_DELETE_MEMO = HOST_URL + "/memos/garbage";
        // 공유메모
        SERVER_URL_SELECT_SHARE_MEMO = HOST_URL + "/memos?user_id=";
        SERVER_URL_SEARCH_USER = HOST_URL + "/users?email=";
        SERVER_URL_INSERT_SHARE_USER = HOST_URL + "/sharings";
        SERVER_URL_UPDATE_SHARE_MEMO_PIN = HOST_URL + "/memos/shared/pin";
        SERVER_URL_CANCEL_SHARE_MEMO = HOST_URL + "/sharings";
        SERVER_URL_RECENT_SHARE_USER = HOST_URL + "/sharings/users/";
        // 베스트메모
        SERVER_URL_BEST_MEMO = HOST_URL + "/memos/best";
        // 아이데이션
        SERVER_URL_SELECT_IDEATION_MEMO = HOST_URL + "/ideations/";
        SERVER_URL_SELECT_COMBINE_MEMO = HOST_URL + "/combinations/recommendations/";
        SERVER_URL_INSERT_COMBINE_MEMO = HOST_URL + "/combinations";
        SERVER_URL_INSERT_HIDDEN_MEMO = HOST_URL + "/ideations/new";
        // 상세보기
        SERVER_URL_SELECT_DETAIL_MEMO = HOST_URL + "/memos/";
        SERVER_URL_UPDATE_DERAIL_MEMO = HOST_URL + "/memos/";
        SERVER_URL_INSERT_COMMENT = HOST_URL + "/replies";
        SERVER_URL_CANCEL_COMBINE_MEMO = HOST_URL + "/combinations";
        // 키워드
        SERVER_URL_INSERT_KEYWORD = HOST_URL + "/keywords";
        SERVER_URL_SELECT_ALL_KEYWORD = HOST_URL + "/keywords/management?user_id=";
        SERVER_URL_SELECT_USER_KEYWORD = HOST_URL + "/keywords?user_id=";
        SERVER_URL_DELETE_KEYWORD = HOST_URL + "/keywords/";
        SERVER_URL_ATTACH_KEYWORD = HOST_URL + "/memos/keywords";
        // 휴지통
        SERVER_URL_SELECT_DELETE_MEMO = HOST_URL + "/memos/garbage?user_id=";
        SERVER_URL_DELETE_SELECTED_MEMO = HOST_URL + "/memos/garbage";
        SERVER_URL_DELETE_ALL_MEMO = HOST_URL + "/memos/garbage/all";
        // FCM
        SERVER_URL_SEND_FCM_TOKEN = HOST_URL + "/users/device";
    }
}
