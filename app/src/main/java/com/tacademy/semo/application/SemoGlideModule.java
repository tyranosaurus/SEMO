package com.tacademy.semo.application;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.GlideModule;

public class SemoGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // 현재 앱의 전체 가용 메모리 사이즈를 계산해준다.
        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
        int defaultMemoryCacheSize = calculator.getMemoryCacheSize();
        int defaultBitmapPoolSize = calculator.getBitmapPoolSize();

        // 현재 Glide가 관리하는 캐쉬사이즈에 10%를 증가. 아래 1.1 곱하여 10% 센트 증가한 값을 할당함
        int customMemoryCacheSize = (int) (1.1 * defaultMemoryCacheSize);
        int customBitmapPoolSize = (int) (1.1 * defaultBitmapPoolSize);

        // 캐시 사이즈를 받아 Glide에 세팅해준다.
        builder.setMemoryCache(new LruResourceCache(customMemoryCacheSize));
        builder.setBitmapPool(new LruBitmapPool(customBitmapPoolSize));

        //더 선명하게 보여줄 팀은 DecodeFormat.PREFER_ARGB_8888로 설정(메모리소모많음)!!!
        builder.setDecodeFormat(DecodeFormat.PREFER_RGB_565);
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
