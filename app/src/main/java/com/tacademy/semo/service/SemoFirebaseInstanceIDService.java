package com.tacademy.semo.service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.tacademy.semo.application.SemoApplication;
import com.tacademy.semo.network.NetworkDefineConstant;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SemoFirebaseInstanceIDService extends FirebaseInstanceIdService {

    int userId = SemoApplication.getUserId();
    private static final String TAG = "MyFirebaseIIDService";

    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG, "Refreshed token: " + token);

        // 생성등록된 토큰을 개인 앱서버에 보내 저장해 두었다가 추가 뭔가를 하고 싶으면 할 수 있도록 한다.
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("user_id", String.valueOf(userId))
                .add("token", token)
                .build();

        //request
        Request request = new Request.Builder()
                .url(NetworkDefineConstant.SERVER_URL_SEND_FCM_TOKEN)
                .post(body)
                .build();

        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
