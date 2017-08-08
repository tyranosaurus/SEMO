package com.tacademy.semo.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tacademy.semo.R;

public class SplashActivity extends AppCompatActivity {

    // 쉐어드 프리퍼런스
    public SharedPreferences preferencesGuide;
    public SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 쉐어드 프리퍼런스 설정
        preferencesGuide = getSharedPreferences("login", 0);
        editor = preferencesGuide.edit();

        ImageView imageView1 = (ImageView) findViewById(R.id.imageView1);
        ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
        ImageView imageView3 = (ImageView) findViewById(R.id.imageView3);
        LinearLayout linearLayoutMessage = (LinearLayout) findViewById(R.id.linearLayoutMessage);

        ImageView imageViewSe = (ImageView) findViewById(R.id.imageViewSe);
        ImageView imageViewSang = (ImageView) findViewById(R.id.imageViewSang);
        ImageView imageViewUi = (ImageView) findViewById(R.id.imageViewUi);
        ImageView imageViewMo = (ImageView) findViewById(R.id.imageViewMo);
        ImageView imageViewDeun = (ImageView) findViewById(R.id.imageViewDeun);
        ImageView imageViewMe = (ImageView) findViewById(R.id.imageViewMe);
        ImageView imageViewMoEnd = (ImageView) findViewById(R.id.imageViewMoEnd);

        // 애니메이션
        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.alpha);
        imageView1.startAnimation(animation1);

        Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.alpha2);
        imageView2.startAnimation(animation2);

        Animation animation3 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.alpha3);
        imageView3.startAnimation(animation3);

        Animation animation4 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.alpha_text);
        linearLayoutMessage.startAnimation(animation4);

        Animation animation5 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.alpha_text_gone);
        imageViewSang.startAnimation(animation5);
        imageViewUi.startAnimation(animation5);
        imageViewMo.startAnimation(animation5);
        imageViewDeun.startAnimation(animation5);
        imageViewMe.startAnimation(animation5);

        Animation animation6 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.translate_text_se);
        imageViewSe.startAnimation(animation6);

        Animation animation7 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.translate_text_mo_end);
        imageViewMoEnd.startAnimation(animation7);

        // 애니메이션 종료 1초 후 홈 액티비티 전환
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if ( preferencesGuide.getBoolean("autoLogin", false) ) {

                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    startActivity(intent);

                    finish();

                    return;
                }


                startActivity(new Intent(SplashActivity.this, GuideActivity.class));
                finish();
            }
        }, 3000);
    }
}
