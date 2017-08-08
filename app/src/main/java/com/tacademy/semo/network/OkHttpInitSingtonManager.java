package com.tacademy.semo.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class OkHttpInitSingtonManager {

    // 요거 하나를 계속 재사용하려고 싱글톤으로 만든다.
    private static OkHttpClient okHttpClient;

    static{
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS) // 10초
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

   public static OkHttpClient getOkHttpClient(){
       if ( okHttpClient != null ) {
           return okHttpClient;
       } else {
           okHttpClient = new OkHttpClient.Builder()
                   .connectTimeout(15, TimeUnit.SECONDS)
                   .readTimeout(15, TimeUnit.SECONDS)
                   .build();
       }
       return okHttpClient;
   }
}
