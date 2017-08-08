package com.tacademy.semo.application;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class SemoApplication extends Application {

    // 로그인 정보
    private static int userId;
    private static String userNickname;
    private static String userProfile;
    private static String userEmail;
    private static String userPassword;

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        userId = -1;
        userNickname = "";
        userProfile = "";
        userEmail = "";
    }

    public static Context getSemoContext() {

        if ( mContext == null ) {
            Log.e("CONTEXT_ERROR", "SEMO application Context is null");

            return null;
        }

        return mContext;
    }

    // 유저 처리. 세션 배우면 다 지울 것
    public static void setUser(int id, String nickname, String profile, String email, String password) {
        userId = id;
        userNickname = nickname;
        userProfile = profile;
        userEmail = email;
        userPassword = password;
    }

    public static void setUserId(int id) {
        userId = id;
    }

    public static void setUserUserNickname(String nickname) {
        userNickname = nickname;
    }

    public static void setUserUserProfile(String profile) {
        userProfile = profile;
    }

    public static void setUserEmail(String email) {
        userEmail = email;
    }

    public static void setUserPassword(String password) {
        userPassword = password;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getUserNickname() {
        return userNickname;
    }

    public static String getUserProfile() {
        return userProfile;
    }

    public static String getUserEmail() {
        return userEmail;
    }

    public static String getUserPassword() {
        return userPassword;
    }
}
