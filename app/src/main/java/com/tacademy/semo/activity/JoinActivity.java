package com.tacademy.semo.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tacademy.semo.R;
import com.tacademy.semo.application.SemoApplication;

public class JoinActivity extends AppCompatActivity {

    // 쉐어드 프리퍼런스
    public SharedPreferences preferencesGuide;
    public SharedPreferences.Editor editor;

    EditText editTextEmail;
    EditText editTextNickname;
    EditText editTextPassword;
    EditText editTextPassword2;
    TextView textViewCompleteJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        // 쉐어드 프리퍼런스 설정
        preferencesGuide = getSharedPreferences("login", 0);
        editor = preferencesGuide.edit();

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextNickname = (EditText) findViewById(R.id.editTextNickname);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextPassword2 = (EditText) findViewById(R.id.editTextPassword2);
        textViewCompleteJoin = (TextView) findViewById(R.id.textViewCompleteJoin);
        textViewCompleteJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 회원가입 완료 처리
                // 서버에 이메일, 닉네임, 비번 전송하고 응답으로 토큰받아오기
                // postExcute에서 홈액티비티로 인텐트 넘기기
                // 지금은 SemoApplication에서 처리
                if ( editTextPassword.getText().toString().trim().isEmpty() || editTextPassword.getText().toString().trim().equals("") ){
                    Toast.makeText(getApplicationContext(), "패스워드를 입력해주세요", Toast.LENGTH_SHORT).show();

                    return;
                }

                if ( editTextPassword.getText().toString().equals(editTextPassword2.getText().toString()) ) {
                    // 원래 여기를 어싱크 태스크 해야함
                    SemoApplication.setUser(6, editTextNickname.getText().toString(), "", editTextEmail.getText().toString(), editTextPassword.getText().toString());

                    editor.putInt("id", 6);
                    editor.putString("nickname", editTextNickname.getText().toString());
                    editor.putString("email", editTextEmail.getText().toString());
                    editor.putString("password", editTextPassword.getText().toString());
                    editor.commit();

                    Intent intent = new Intent(JoinActivity.this, HomeActivity.class);
                    startActivity(intent);

                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "패스워드가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();

                    return;
                }
            }
        });
    }
}
