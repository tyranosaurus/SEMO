package com.tacademy.semo.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tacademy.semo.R;

public class SettingsActivity extends AppCompatActivity {

    Toolbar mToolbar;
    ActionBar mActionBar;

    ImageView imageViewBack;
    TextView textViewSettings;

    // 계정
    LinearLayout linearLayoutEditProfile;
    LinearLayout linearLayoutManagingAccount;
    //서비스 정보
    LinearLayout linearLayoutNoti;
    LinearLayout linearLayoutGuide;
    LinearLayout linearLayoutAppInfo;
    LinearLayout linearLayoutDeleteMember;
    // 로그아웃
    Button buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 프로필 편집
        linearLayoutEditProfile = (LinearLayout) findViewById(R.id.linearLayoutEditProfile);
        linearLayoutEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "준비중입니다!", Toast.LENGTH_SHORT).show();
            }
        });
        // 연동 계정 관리
        linearLayoutManagingAccount = (LinearLayout) findViewById(R.id.linearLayoutManagingAccount);
        linearLayoutManagingAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "준비중입니다!", Toast.LENGTH_SHORT).show();
            }
        });

        // 공지사항
        linearLayoutNoti = (LinearLayout) findViewById(R.id.linearLayoutNoti);
        linearLayoutNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "준비중입니다!", Toast.LENGTH_SHORT).show();
            }
        });
        // 가이드
        linearLayoutGuide = (LinearLayout) findViewById(R.id.linearLayoutGuide);
        linearLayoutGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 가이드 다시 띄움
                Toast.makeText(getApplicationContext(), "현재 준비중입니다.", Toast.LENGTH_SHORT).show();
            }
        });
        // 앱 정보
        linearLayoutAppInfo = (LinearLayout) findViewById(R.id.linearLayoutAppInfo);
        linearLayoutAppInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "앱 버전 : Version 1.0", Toast.LENGTH_SHORT).show();
            }
        });
        // 회원탈퇴
        linearLayoutDeleteMember = (LinearLayout) findViewById(R.id.linearLayoutDeleteMember);
        linearLayoutDeleteMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "탈퇴되었습니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(SettingsActivity.this);
            }
        });
        // 로그아웃
        buttonLogout = (Button) findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "로그아웃 되었습니다", Toast.LENGTH_SHORT).show();
                ActivityCompat.finishAffinity(SettingsActivity.this);
            }
        });

        // 툴바 설정
        mToolbar = (Toolbar) findViewById(R.id.toolbarSetting);
        setSupportActionBar(mToolbar);

        mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        // 툴바에 사용할 커스텀 레이아웃 인플레이션
        View viewToolBar = getLayoutInflater().inflate(R.layout.toolbar_settings, null);

        imageViewBack = (ImageView) viewToolBar.findViewById(R.id.imageViewBack);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        textViewSettings = (TextView) viewToolBar.findViewById(R.id.textViewSettings);

        // 인플레이션한 커스텀 툴바 세팅
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        mActionBar.setCustomView(viewToolBar, layoutParams);
    }

    public class MyMemoDialog extends Dialog {
        FrameLayout frameLayoutBackground;

        public MyMemoDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_my_memo);

            frameLayoutBackground = (FrameLayout) findViewById(R.id.frameLayoutBackground);
            frameLayoutBackground.setBackgroundResource(R.drawable.dialog_my_memo);
            Button buttonComplete = (Button) findViewById(R.id.buttonComplete);
            buttonComplete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

        }
    }

    public class ShareMemoDialog extends Dialog {
        FrameLayout frameLayoutBackground;

        public ShareMemoDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_my_memo);

            frameLayoutBackground = (FrameLayout) findViewById(R.id.frameLayoutBackground);
            frameLayoutBackground.setBackgroundResource(R.drawable.dialog_sharememo);
            Button buttonComplete = (Button) findViewById(R.id.buttonComplete);
            buttonComplete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

        }
    }

    public class KeywordDialog extends Dialog {
        FrameLayout frameLayoutBackground;

        public KeywordDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_my_memo);

            frameLayoutBackground = (FrameLayout) findViewById(R.id.frameLayoutBackground);
            frameLayoutBackground.setBackgroundResource(R.drawable.dialog_keyword);
            Button buttonComplete = (Button) findViewById(R.id.buttonComplete);
            buttonComplete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dismiss();
                }
            });

        }
    }
}
